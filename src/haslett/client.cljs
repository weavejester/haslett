(ns haslett.client
  (:require [cljs.core.async :as a :refer [<! >!]]
            [cljs.reader :as edn]
            [cognitect.transit :as transit])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn- chan-fn [xf]
  (fn chan
    ([]                          (chan 1))
    ([buf-or-n]                  (chan buf-or-n identity))
    ([buf-or-n xform]            (chan buf-or-n xform nil))
    ([buf-or-n xform ex-handler] (a/chan buf-or-n (comp xf xform) ex-handler))))

(def edn-source     (chan-fn (map edn/read-string)))
(def edn-sink       (chan-fn (map pr-str)))

(def transit-source (chan-fn (map #(transit/read (transit/reader :json) %))))
(def transit-sink   (chan-fn (map #(transit/write (transit/writer :json) %))))

(def json-source    (chan-fn (map #(js->clj (js/JSON.parse %)))))
(def json-sink      (chan-fn (map #(js/JSON.stringify (clj->js %)))))

(defn websocket [url]
  (js/WebSocket. url))

(defn close [socket]
  (.close socket))

(defn connect
  ([socket]
   (connect socket (edn-source) (edn-sink)))
  ([socket source sink]
   (let [return (a/promise-chan)]
     (aset socket "onopen"    (fn [_] (a/put! return {:source source, :sink sink})))
     (aset socket "onclose"   (fn [_] (a/close! source) (a/close! sink)))
     (aset socket "onmessage" (fn [e] (a/put! source (.-data e))))
     (go-loop []
       (if-let [msg (<! sink)]
         (do (.send socket msg) (recur))
         (close socket)))
     return)))
