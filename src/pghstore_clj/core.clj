(ns pghstore-clj.core
  (:require [clojure.string :as st])
  (:import [org.postgresql.util PGobject]))

(defprotocol THstorable
  (to-hstore [this]))

(defprotocol FHstorable
  (from-hstore [this]))

(defn- escape-sql-string [s]
  (st/escape (str s) {\" "\\\""}))

(defn- create-key-value-pair [k v]
  (format "\"%s\"=>\"%s\"" (name k) (escape-sql-string v)))

(defn- get-string-between-quotes [s]
  (subs s 1 (- (.length s) 1)))

(extend-type clojure.lang.IPersistentMap
  THstorable
  (to-hstore [this]
    (doto (PGobject.)
      (.setType "hstore")
      (.setValue
        (apply str
               (interpose ", "
                          (for [[k v] this] (create-key-value-pair k v))))))))

(extend-type org.postgresql.util.PGobject
  FHstorable
  (from-hstore [this]
    (when (= (.getType this) "hstore")
      (into {}
            (for [[k v]
                  (map (fn [v]
                         (map get-string-between-quotes (st/split v #"=>")))
                       (st/split (.getValue this) #", "))]
              [(keyword k) v])))))

