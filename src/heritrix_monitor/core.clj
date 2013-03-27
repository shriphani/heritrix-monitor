(ns heritrix-monitor.core
	(:gen-class :main true)
	(:use clojure.tools.cli)
	(:use [clojure.java.io :only (file writer reader)])
	(:require warc-clojure.core)
	(:require clojure.data.csv)
	(:require clojure.set)
	(:use (incanter core stats charts))
	(:import java.text.SimpleDateFormat)
	(:require [me.raynes.fs :as fs])
	(:use [clojure.string :only (join)]))

;(def *stats-out-dir* "/bos/www/htdocs/spalakod")

(def *stats-out-dir* "/tmp/")

(defn simple-count-stats
	"Computes number of warcs and size of all response records downloaded"
	[warc-gz-fname]
	(let [warc-gz-seq (warc-clojure.core/get-response-records-seq (warc-clojure.core/get-warc-reader warc-gz-fname))]
		(reduce 
			(fn 
				[x y] 
				(merge x 
				{:count (+' (:count x) 1) 
				 :size (+' (:content-length y) (:size x))})) 
			{:count 0 :size 0} 
			warc-gz-seq)))

(defn total-warc-gz-stats
	"Computes number of warcs etc but is not really used."
	[warc-gz-fname]
	(let [warc-gz-seq (warc-clojure.core/get-records-seq (warc-clojure.core/get-warc-reader warc-gz-fname))]
		(reduce
			(fn
				[x y]
				(merge x
					{:count (+' (:count x) 1)
					 :size (+' (:content-length y) (:size x))}))
			{:count 0 :size 0}
			warc-gz-seq)))

(defn add-to-csv-and-plot
	([a-row heritrix-job-dir] (add-to-csv-and-plot a-row heritrix-job-dir *stats-out-dir*))
 	([a-row heritrix-job-dir stats-out-dir]
	 	(let [plot-size (fn 
	 						[records] 
	 						(save 
	 							(time-series-plot 
	 								(map (fn [record] (read-string (first record))) records)
	 								(map (fn [record] (read-string (second record))) records)
	 								:x-label "Time"
	 								:y-label "Size in Bytes")
	 							(join "" [stats-out-dir "size-graph.png"])))
	 		  plot-count (fn
	 		  				[records]
	 		  				(save
	 		  					(time-series-plot
	 		  						(map (fn [record] (read-string (first record))) records)
	 		  						(map (fn [record] (read-string (nth record 2))) records)
	 		  						:x-label "Time"
	 		  						:y-label "Number of Response Records")
	 		  					(join "" [stats-out-dir "count-graph.png"])))

	 		  csv-file-name (join 
	 							"" 
	 							[stats-out-dir
	 							 (join "" [(fs/base-name heritrix-job-dir) ".csv"])])

	 		  html-file-name (join
	 		  					""
	 		  					[stats-out-dir
	 		  					 (join "" [(fs/base-name heritrix-job-dir) ".html"])])]

	 		(with-open [csv-file-handle (writer csv-file-name :append true)]
	 			(clojure.data.csv/write-csv csv-file-handle a-row))
	 		
	 		(with-open [csv-file-handle (reader csv-file-name)]
	 			(let [records (clojure.data.csv/read-csv csv-file-handle)]
	 				(plot-count records)
	 				(plot-size records)))

	 		(with-open [html-file-handle (writer html-file-name)]
	 			(.write html-file-handle (join 
	 										""
	 										[
	 											"<html>
	 												<title>"

	 											(fs/base-name heritrix-job-dir)

	 											"</title>
	 											<p>Count Graph<br/>
	 											<img src=count-graph.png /></p>
	 											<p>Size-graph<br/>
	 											<img src=size-graph.png /></p>
	 											</html>"]))))))


(defn -main
	[& args]
	(let 
		[[args-vector [heritrix-job-dir] banner] (cli 
													args 
													["-o" 
													 "--output-dir" 
													 "Specify output directory" 
													 :default *stats-out-dir*])
		 stats (reduce 
						(fn
							[x y]
							(merge x
							{
								:count (+ (:count x) (:count y))
								:size (+ (:size y) (:size x))
							}))
						{:count 0 :size 0}
						(map 
							simple-count-stats 
							(filter 
								#(or (.endsWith (.getName %) ".warc.gz") (.endsWith (.getName %) ".warc.gz.open"))
								(file-seq (file heritrix-job-dir)))))]
	
		(add-to-csv-and-plot 
			(vector (vector (.getTime (new java.util.Date)) (:size stats) (:count stats) ""))
			heritrix-job-dir (:output-dir args-vector))))
