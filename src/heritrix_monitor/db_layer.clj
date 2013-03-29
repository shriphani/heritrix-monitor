(ns heritrix-monitor.db-layer
	(:require [clojure.java.jdbc :as sql])
	(:use [clojure.string :only (join)])
	(:require [clojure.contrib.java-utils :as utils]))

(defn create-when-empty
	[db]
	(when (empty? (sql/with-connection 
					db 
					(into [] (resultset-seq (-> (sql/connection) 
												(.getMetaData) 
												(.getTables nil nil nil (into-array ["TABLE" "VIEW"])))))))

		(sql/with-connection db
			(sql/create-table :warc_gz_files
							[:job_name 		   :text]
							[:warc_gz_filename :text]))
		(sql/with-connection db
			(sql/create-table :seed_urls
							[:job_name 	:text]
							[:url  		:text]))))

(defn add-warc-filename
	[warc-filename job-name db]
	(sql/with-connection db
		(sql/insert-records :warc_gz_files {:job_name job-name :warc_gz_filename warc-filename})))

(defn add-urls-filename
	[urls job-name db]
	(sql/with-connection db
		(doseq [url urls]
			(sql/insert-records :seed_urls {:job_name job-name :url url}))))

(defn already-visited?
	[warc-filename job-name db]
	(not (empty? (sql/with-connection db
				(sql/with-query-results rs  [(join "" [
														"select * from warc_gz_files where job_name=\"" 
														job-name 
														"\"" 
														"AND warc_gz_filename=\""
														warc-filename
														"\";"
												  	  ])] (doall rs))))))
