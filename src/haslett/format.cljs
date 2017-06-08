(ns haslett.format
  "A namespace containing formatters that read and write information from
  WebSocket streams. Used with haslett.client/connect."
  (:refer-clojure :exclude [identity])
  (:require [cljs.reader :as edn]
            [cognitect.transit :as transit]))

(defprotocol Format
  "The format protocol."
  (read  [formatter string])
  (write [formatter value]))

(def identity
  "The identity formatter. Does nothing to the input or output."
  (reify Format
    (read  [_ s] s)
    (write [_ v] v)))

(def transit
  "Read and write data encoded in transit+json."
  (reify Format
    (read  [_ s] (transit/read (transit/reader :json) s))
    (write [_ v] (transit/write (transit/writer :json) v))))

(def edn
  "Read and write data encoded in edn."
  (reify Format
    (read  [_ s] (edn/read-string s))
    (write [_ v] (pr-str v))))

(def json
  "Read and write data encoded in JSON."
  (reify Format
    (read  [_ s] (js->clj (js/JSON.parse s)))
    (write [_ v] (js/JSON.stringify (clj->js v)))))
