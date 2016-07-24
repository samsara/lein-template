(ns leiningen.new.samsara
  (:require [leiningen.new.templates :refer [renderer name-to-path ->files]]
            [leiningen.core.main :as main]))

(def ^:const RELEASE "0.5.5.0")

(def ^:const SNAPSHOT "0.4.4.0-SNAPSHOT")

(def ^:const SUPPORTED-OPTIONS ["--with-snapshot" "--with-version"])

(def render (renderer "samsara"))

(defn parse-version
  [args options]
  (cond
    (contains? args :--with-version) (assoc options :version (:--with-version args) :docker-version (:--with-version args))
    (contains? args :--with-snapshot) (assoc options :version SNAPSHOT :docker-version "snapshot")
    :else (assoc options :version RELEASE :docker-version RELEASE)))

(defn validate-arguments
  [args]
  (let [opt-keys (keys args)]
    (when (every? (apply hash-set (map keyword SUPPORTED-OPTIONS)) opt-keys)
      (do (when (every? (apply hash-set opt-keys) [:--with-version :--with-snapshot])
            (main/warn "Both --with-version and --with-snapshot arguments were found. Samsara will use the --with-version value."))
          (parse-version args {})))))

(defn samsara
  "A leinengen template to get you started with Samsara. Sets up a simple processor pipeline, required config files, and
  a docker-compose file to get you up and running quickly."
  [name & options]
  (let [[parsed-options _] (main/parse-options options)]
    (if-let [parsed (validate-arguments parsed-options)]
      (let [data {:name           name
                  :sanitized      (name-to-path name)
                  :version        (:version parsed)
                  :docker-version (:docker-version parsed)}]
        (main/info "Generating fresh 'lein new' samsara project.")
        (->files data
                 ["project.clj"                      (render "project.clj" data)]
                 ["src/{{sanitized}}/main.clj"       (render "main.clj" data)]
                 ["src/{{sanitized}}/core.clj"       (render "core.clj" data)]
                 ["test/{{sanitized}}/core_test.clj" (render "core_test.clj" data)]
                 ["config/config.edn"                (render "config.edn" data)]
                 ["docker-compose.yml"               (render "docker-compose.yml" data)]
                 ["README.md"                        (render "README.md" data)]
                 ["CHANGELOG.md"                     (render "CHANGELOG.md" data)]
                 ["LICENSE"                          (render "LICENSE")]
                 ["doc/intro.md"                     (render "intro.md")]
                 [".gitignore"                       (render ".gitignore")]
                 [".hgignore"                      (render ".hgignore")]
                 "resources"))
      (apply main/info "Invalid arguments. The Samsara template supports the following arguments: " SUPPORTED-OPTIONS))))
