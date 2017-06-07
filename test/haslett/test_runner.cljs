(ns haslett.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [haslett.core-test]))

(doo-tests 'haslett.core-test)
