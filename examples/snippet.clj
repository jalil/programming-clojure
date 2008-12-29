(ns examples.snippet
  (:require [clojure.contrib.sql :as sql]))

(def db {:classname "org.hsqldb.jdbcDriver"
         :subprotocol "hsqldb"
         :subname "mem:testdb"})

(defn drop-snippets []
  (try
   (sql/drop-table :snippets)
   (catch Exception e)))

(defn create-snippets []
  (sql/create-table :snippets
    [:id :int "IDENTITY" "PRIMARY KEY"]
    [:body :varchar "NOT NULL"]
    [:created_at :datetime]))

(defn insert-snippets []
  (let [now (java.sql.Timestamp. (.getTime (java.util.Date.)))]
    (sql/insert-values :snippets
      [:body :created_at]		     
      ["The quick brown fox jumped over the lazy dog" now]
      ["All cows eat grass" now])))

(defn sample-snippets []
  (sql/with-connection db
    (sql/transaction
     (drop-snippets)
     (create-snippets)
     (insert-snippets))))

(defn reset-snippets []
  (sql/with-connection db
    (sql/transaction
     (drop-snippets)
     (insert-snippets))))

(defn read-snippets []
  (sql/with-connection db
    (sql/with-results res
     "select * from snippets"
      (doseq [rec res]
        (println rec)))))

(defn sql-query [q]
  (sql/with-results res q (into [] res)))
		       
(defn insert-snippet [params]
  (sql/with-connection db
    (sql/transaction
     (let [now (java.sql.Timestamp. (.getTime (java.util.Date.)))]
       (sql/insert-values :snippets
         [:body :created_at]
	 [(:body params) now])
       ; extract id from hsqldb's [{:@p0 id}]
       (first (vals (first (sql-query "CALL IDENTITY()"))))))))
