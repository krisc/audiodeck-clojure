(ns dispatcher
  (:import (org.puredata.core.utils PdDispatcher))
  (:gen-class
  ;;:name dispatcher
    :extends org.puredata.core.utils.PdDispatcher))

(defn -print [this s] (println s))
