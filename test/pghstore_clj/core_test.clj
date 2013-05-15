(ns pghstore-clj.core-test
  (:use clojure.test
        pghstore-clj.core))

(deftest test-hash-to-hstore
  (testing "creating a PGobject with the correct value"
    (is (= "hstore" (.getType (to-hstore {:color "blue"})))))
  (testing "storing the correct value in the PGobject"
    (is (= "\"name\"=>\"test\", \"username\"=>\"root\""
           (.getValue (to-hstore {:name "test" :username "root"}))))))

(deftest test-hstore-to-hash
  (let [pgo (to-hstore {:name "test" :username "root"})]
    (testing "creating a hash from a PGobject"
      (is (= {:name "test" :username "root"} (from-hstore pgo))))
    (testing "doesn't create a hash if the type isn't hstore"
      (is (= nil (from-hstore (doto pgo (.setType "blah"))))))))

(deftest test-escaping-double-quote
  (let [pgo (to-hstore {:name "te\"st" :username "ro\"ot"})]
    (println (str pgo))
    (testing "saving a string with a double quote in it"
      (is (= {:name "te\\\"st" :username "ro\\\"ot"} (from-hstore pgo))))))

