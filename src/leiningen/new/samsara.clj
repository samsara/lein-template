(ns leiningen.new.samsara
  (:require [leiningen.new.templates :refer [renderer name-to-path ->files]]
            [leiningen.core.main :as main]
            [clj-http.client :as http]))

(def ^:const SUPPORTED-OPTIONS ["--with-snapshot" "--with-version"])

(def ^:const CLOJARS_URL "https://clojars.org/api/artifacts/samsara/samsara-core")

(def render (renderer "samsara"))

(defn validate-arguments
  [args]
  (cond
    (not (every? (apply hash-set (map keyword SUPPORTED-OPTIONS)) (keys args)))
    {:error (apply str "Invalid arguments. The Samsara template supports the following arguments: " (interpose ", " SUPPORTED-OPTIONS))}
    (if-let [version (:--with-version args)]
            (let [versions (:recent_versions (read-string (:body (http/get CLOJARS_URL {:accept :edn}))))]
              (not (some #(= (:version %) version) versions))))
    {:error "Invalid Samsara version."}))

(defn get-latest-version
  [key]
  (key (read-string (:body (http/get CLOJARS_URL {:accept :edn})))))

(defn parse-version
  [args options]
  (cond
    (contains? args :--with-version) (assoc options :version (:--with-version args) :docker-version (:--with-version args))
    (contains? args :--with-snapshot) (assoc options :version (get-latest-version :latest_version) :docker-version "snapshot")
    :else (let [latest-release (get-latest-version :latest_release)] (assoc options :version latest-release :docker-version latest-release))))

(defn validate-and-parse-arguments
  "Verifies that all template specific arguments supplied to the lein new command are valid options. Also checks to see
   if there is both a --with-version and --with-snapshot, in which case it uses the --with-version option"
  [args]
  (if-let [errors (validate-arguments args)]
    errors
    (do (when (every? (apply hash-set (keys args)) [:--with-version :--with-snapshot])
          (main/info "Both --with-version and --with-snapshot arguments were found. Samsara will use the --with-version value."))
        (parse-version args {}))))

(defn samsara
  "A leinengen template to get you started with Samsara. Sets up a simple processor pipeline, required config files, and
  a docker-compose file to get you up and running quickly."
  [name & options]
  (let [parsed (validate-and-parse-arguments (get (main/parse-options options) 0))]
    (if (not (:error parsed))
      (let [data {:name           name
                  :sanitized      (name-to-path name)
                  :version        (:version parsed)
                  :docker-version (:docker-version parsed)}]
        (main/info "Generating fresh 'lein new' samsara project.")
        (->files data
                 ["project.clj" (render "project.clj" data)]
                 ["src/{{sanitized}}/main.clj" (render "main.clj" data)]
                 ["src/{{sanitized}}/core.clj" (render "core.clj" data)]
                 ["test/{{sanitized}}/core_test.clj" (render "core_test.clj" data)]
                 ["config/config.edn" (render "config.edn" data)]
                 ["docker-compose.yml" (render "docker-compose.yml" data)]
                 ["CHANGELOG.md" (render "CHANGELOG.md" data)]
                 ["LICENSE" (render "LICENSE")]
                 ["doc/intro.md" (render "intro.md")]
                 [".gitignore" (render ".gitignore")]
                 [".hgignore" (render ".hgignore")]
                 "resources"))
      (main/info (:error parsed)))))
