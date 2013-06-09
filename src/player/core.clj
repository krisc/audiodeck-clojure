(ns player.core
  (:import (java.io IOException)
           (org.puredata.core PdBase PdListener)
           (org.puredata.core.utils PdDispatcher)))

;; deck object
(defn deck []
  (let [playlist (atom []) current-pos (atom 0)
        playing (atom false) patch (atom 0)
        audioThread (new JavaSoundThread 44100 2 16)]

    (defn open-patch []
      (reset! patch (PdBase/openPatch "src/pd/deck.pd"))
      (.start audioThread))

    (defn close-patch []
      (.interrupt audioThread)
      (.join audioThread)
      (PdBase/closePatch @patch))

    ;; change to add a list of files
    (defn add! [files]
      (swap! playlist conj files))

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

    (defn ctl-resume []
      (PdBase/sendBang "ctl-resume"))

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

    (defn ctl-seek! [fun]
      (swap! current-pos fun 1)
      (load-new-pos))

    (defn ctl-next! []
      (if (= (inc @current-pos) (count @playlist))
        (do
          (reset! current-pos 0)
          (load-new-pos))
        (ctl-seek! +)))

    (defn ctl-prev! []
      (if (< (dec @current-pos) 0)
        (do
          (reset! current-pos (dec (count @playlist)))
          (load-new-pos))
        (ctl-seek! -)))

    (defn playing? []
      (if (= @playing true)
        true
        false))

    (defn dispatch [m & args]
      (cond
        (= m 'add!) (add! (first args))
        (= m 'pos) (print-current-pos)
        (= m 'list) (print-playlist)
        (= m 'play) (ctl-play)
        (= m 'stop) (ctl-stop)
        (= m 'resume) (ctl-resume)
        (= m 'pause) (ctl-pause)
        (= m 'next!) (ctl-next!)
        (= m 'prev!) (ctl-prev!)
        (= m 'playing?) (playing?)
        :else (println "Error: Unknown operation -- deck")))

    ;;listen for end-of-clip bang
    (def my-dispatcher (new dispatcher))
    (PdBase/setReceiver my-dispatcher)
    (defn my-listener [] (proxy [PdListener] []
      (receiveBang [source]
        (do
          (println "FUCKING PLAYING FUCKE YOU!!!!!11")))))
    (. my-dispatcher addListener "playing" (my-listener)))

    (open-patch)
    dispatch
)



(defn -main [& args]
  (def deck0 (deck))
  (println "New deck: deck0"))
