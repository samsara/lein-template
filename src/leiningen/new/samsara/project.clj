(defproject {{name}} "0.1.0-SNAPSHOT"
  :description "FIXME: write description"

  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"

  :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [samsara/samsara-core "{{version}}"]]

  :main {{name}}.main

  :profiles {:uberjar {:aot :all}
             :dev     {:dependencies [[midje "1.7.0"]]
                       :plugins      [[lein-midje "3.1.3"]
                                      [lein-binplus "0.4.1"]]}}
  )