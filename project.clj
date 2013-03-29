(defproject heritrix-monitor "0.1.0-SNAPSHOT"
  :description "Plots Stats About Crawl"
  :url "http://github.com/lemurproject"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
  				       [org.clojure/tools.cli "0.2.2"]
  				       [org.clojars.shriphani/warc-clojure "0.2.1-SNAPSHOT"]
  				       [org.clojure/data.csv "0.1.2"]
  				       [me.raynes/fs "1.4.0"]
  				       [incanter "1.2.3-SNAPSHOT"]
                 [org.clojure/java.jdbc "0.0.6"]
                 [org.xerial/sqlite-jdbc "3.7.2"]]
  :main heritrix-monitor.core)
