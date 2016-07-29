(ns leiningen.new.samsara
  (:require [leiningen.new.templates :refer [renderer name-to-path ->files]]
            [leiningen.core.main :as main]
            [clj-http.client :as http]
            [clojure.string :as str]))

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


(defn sort-by-semantic-version [versions]
  (->> versions
       (map (partial re-find #"(\d+)\.(\d+)\.(\d+)(?:\.(\d+))?(?:-(SNAPSHOT))?"))
       (map (fn [[v a b c d sn]]
              (let [->int (fnil read-string "-1")]
                [(->int a) (->int b) (->int c) (->int d) (not= sn "SNAPSHOT") v])))
       sort
       (map last)))


(defn samsara-versions
  []
  (main/info "Searching for latest Samsara's version on Clojars.org ...")
  (->> (read-string (:body (http/get CLOJARS_URL {:accept :edn})))
       :recent_versions
       (map :version)))


(defn latest-version
  [versions snapshot?]
  (->> versions
       sort-by-semantic-version
       (filter #(if snapshot? true (not (re-find #"SNAPSHOT" %))))
       last))


(defn samsara-latest-version [snapshot?]
  (let [latest (latest-version (samsara-versions) snapshot?)]
    (main/info "Samsara latest version:" latest)
    latest))


(defn parse-version
  [args options]
  (cond
    (contains? args :--with-version) (assoc options :version (:--with-version args) :docker-version (:--with-version args))
    (contains? args :--with-snapshot) (assoc options :version (samsara-latest-version true) :docker-version "snapshot")
    :else (let [latest-release (samsara-latest-version false)]
            (assoc options :version latest-release :docker-version latest-release))))


(defn validate-and-parse-arguments
  "Verifies that all template specific arguments supplied to the lein new command are valid options. Also checks to see
   if there is both a --with-version and --with-snapshot, in which case it uses the --with-version option"
  [args]
  (if-let [errors (validate-arguments args)]
    errors
    (do (when (every? (apply hash-set (keys args)) [:--with-version :--with-snapshot])
          (main/info "Both --with-version and --with-snapshot arguments were found. Samsara will use the --with-version value."))
        (parse-version args {}))))


(defn headline []
  (main/info
   "
-----------------------------------------------
   _____
  / ___/____ _____ ___  _________ __________ _
  \\__ \\/ __ `/ __ `__ \\/ ___/ __ `/ ___/ __ `/
 ___/ / /_/ / / / / / (__  ) /_/ / /  / /_/ /
/____/\\__,_/_/ /_/ /_/____/\\__,_/_/   \\__,_/

-----------------------------------------------
"))

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
        (headline)
        (main/info "Generating fresh Samsara project.")
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
                 #_[".hgignore" (render ".hgignore")] ;; missing
                 "resources")
        (main/info "All done.\n")
        (main/info "To build and test:\n\t$ lein do clean, midje, bin")
        (main/info "To run:\n\t$ docker-compose up -d\n\t$ lein run -- config/config.edn\n")
        )
      (main/info (:error parsed)))))
