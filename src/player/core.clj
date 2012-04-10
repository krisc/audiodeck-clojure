(ns player.core
  (:import (java.io IOException)
           (org.puredata.core PdBase)))

(def audioThread (new JavaSoundThread 44100 2 16))
(def patch (atom 0))

;; Player state variables
(def playing (atom false))
(def playlist (atom []))
(def current-pos (atom 0))

;;Internal functions
(defn playing? []
  (if (= @playing true)
    true
    false))

(defn open-patch []
  (reset! patch (PdBase/openPatch "src/pd/deck.pd"))
  (.start audioThread)
  )

(defn close-patch []
  (.interrupt audioThread)
  (.join audioThread)
  (PdBase/closePatch @patch))


;;Player Controls

(defn load-clip [filename]
  (println "loading clip...")
  (PdBase/sendBang "ctl-stop")
  (if (= (PdBase/sendSymbol  "ctl-loadclip" filename) 0)
    (println "Clip loaded")
    (println "Error while loading clip")))

(defn ctl-stop []
  (reset! playing false)
  (if (= (PdBase/sendBang  "ctl-stop") 0)
    (println "Stopped")
    (println "Error while stopping")))

;; change boolean logic here!
(defn ctl-play []
  (println "playing")
  (if (= @playing false)
    (do (load-clip (get @playlist @current-pos))
        (reset! playing true)))
  (PdBase/sendBang "ctl-play"))

(defn ctl-pause []
  (PdBase/sendBang "ctl-pause"))


;; Playlist Controls

(defn add [filename]
  (swap! playlist conj filename))

(defn print-current-pos []
  (println (str "Current Position: " @current-pos)))

(defn print-playlist []
  (println @playlist))

(defn load-new-pos []
  (if (= @playing true)
    (do
      (load-clip (get @playlist @current-pos))
      (ctl-play))
    (load-clip (get @playlist @current-pos)))
  (print-current-pos))

(defn ctl-seek [fun]
  (swap! current-pos fun 1)
  (load-new-pos))

(defn ctl-next []
  (if (= (inc @current-pos) (count @playlist))
    (do
      (reset! current-pos 0)
      (load-new-pos))
    (ctl-seek +)))

(defn ctl-prev []
  (if (< (dec @current-pos) 0)
    (do
      (reset! current-pos (dec (count @playlist)))
      (load-new-pos))
    (ctl-seek -)))

(defn -main [& args]
  (open-patch)
  (Thread/sleep 5000)
  (close-patch))
