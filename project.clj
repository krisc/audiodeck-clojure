(defproject sample-libpd "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.3.0"]]
  :source-paths ["src" "src/player"]
  :aot [dispatcher]
  :java-source-paths ["src/java"]
  ;;:main player.core
  :jvm-opts [~(str "-Djava.library.path=native/:" (System/getenv "LD_LIBRARY_PATH"))]
)
