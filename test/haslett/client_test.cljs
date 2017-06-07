(ns haslett.client-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [haslett.client :as haslett]))

(deftest test-ws
  (let [events (atom [])]
    (haslett/websocket
     "ws://echo.websocket.org"
     {:on-open    (fn [ws]
                    (swap! events conj [:open])
                    (haslett/send ws "Hello World"))
      :on-message (fn [ws data]
                    (swap! events conj [:message])
                    (haslett/close ws))
      :on-close   (fn [ws]
                    (swap! events conj [:close]))})
    (js/setTimeout
     #(is (= @events [[:open]
                      [:message "Hello World"]
                      [:close]]))
     1000)))
