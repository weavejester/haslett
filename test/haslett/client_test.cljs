(ns haslett.client-test
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [cljs.core.async :as a :refer [<! >! go]]
            [cljs.core.async.impl.protocols :as ap]
            [haslett.client :as ws]
            [haslett.format :as fmt]))

(deftest test-defaults
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3200"))]
          (is (ws/connected? stream))
          (>! (:out stream) "Hello World")
          (is (= (<! (:in stream)) "Hello World"))
          (ws/close stream)
          (done))))) 

(deftest test-transit
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3200" {:format fmt/transit}))]
          (>! (:out stream) {:hello "World"})
          (is (= (<! (:in stream)) {:hello "World"}))
          (ws/close stream)
          (done)))))

(deftest test-edn
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3200" {:format fmt/edn}))]
          (>! (:out stream) {:hello "World"})
          (is (= (<! (:in stream)) {:hello "World"}))
          (ws/close stream)
          (done)))))

(deftest test-json
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3200" {:format fmt/json}))]
          (>! (:out stream) {:hello "World"})
          (is (= (<! (:in stream)) {"hello" "World"}))
          (ws/close stream)
          (done)))))

(deftest test-close
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3200"))]
          (is (= (<! (ws/close stream))      {:code 1000, :reason "Closed by creator"}))
          (is (= (<! (:close-status stream)) {:code 1000, :reason "Closed by creator"}))
          (done)))))

(deftest test-connection-fail
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3201"))]
          (is (not (ws/connected? stream)))
          (is (ap/closed? (:out stream)))
          (is (ap/closed? (:in stream)))
          (is (= (<! (:close-status stream)) {:code 1006, :reason ""}))
          (done)))))

(deftest test-local-close
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3200"))]
          (a/close! (:out stream))
          (is (= (<! (:close-status stream)) {:code 1000, :reason "Closed by creator"}))
          (is (ap/closed? (:in stream)))
          (done)))))

(deftest test-chans-not-closed
  (async done
    (go (let [stream (<! (ws/connect "ws://localhost:3200" {:close-chan? false}))]
          (ws/close stream)
          (is (= (<! (:close-status stream)) {:code 1000, :reason "Closed by creator"}))
          (is (not (ap/closed? (:out stream))))
          (is (not (ap/closed? (:in stream))))
          (done)))))
