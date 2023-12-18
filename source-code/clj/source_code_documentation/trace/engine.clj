
(ns source-code-documentation.trace.engine
    (:require [fruits.vector.api                     :as vector]
              [source-code-documentation.trace.utils :as trace.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn trace-redirection
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) section
  ; @param (map) content-block
  ;
  ; @usage
  ; (trace-redirection [...] {...} {...}
  ;                    {:name "my-function" :type :defn :content [{:type :redirect :meta ["..."] :indent 1}]}
  ;                    {:type :redirect :meta ["..."] :indent 1})
  ; =>
  ; {:type :redirect :meta ["..."] :indent 1 :pointer "another-namespace/another-function"}
  ;
  ; @return (map)
  [_ _ file-data section content-block]
  (let [pointer (trace.utils/derive-pointer                 file-data section content-block)
        pointer (trace.utils/invoke-pointer-namespace-alias file-data section pointer)]
       (assoc content-block :pointer pointer)))

(defn trace-link
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) section
  ; @param (map) content-block
  ;
  ; @return (map)
  [state options file-data section content-block]
  ; Tracing links is the same process as tracing redirections.
  (trace-redirection state options file-data section content-block))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn trace-section
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) section
  ;
  ; @usage
  ; (trace-section [...] {...} {...}
  ;                {:name "my-function" :type :defn :content [{:type :redirect :meta ["..."] :indent 1}]})
  ; =>
  ; {:name    "my-function"
  ;  :content [{:type :redirect :meta ["..."] :indent 1 :pointer "another-namespace/another-function"}]}
  ;
  ; @return (map)
  [state options file-data section]
  (letfn [(f0 [%] (-> % :type (= :redirect)))
          (f1 [%] (-> % :type (= :link)))
          (f2 [%] (trace-redirection state options file-data section %))
          (f3 [%] (trace-link        state options file-data section %))]
         (-> section (update :content vector/->items-by f0 f2)
                     (update :content vector/->items-by f1 f3))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn trace-imported-file
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (maps in vector)
  [state options file-data]
  (letfn [(f0 [%] (trace-section state options file-data %))]
         (update file-data :sections vector/->items f0)))

(defn trace-imported-files
  ; @ignore
  ;
  ; @description
  ; - Traces the links and redirections in documentation contents of defs and defns creating pointers (for all source files within the given source directories).
  ; - Although the documentation generator creates documentation only for files that match the provided (or default)
  ;   filename pattern, to handle documentation content links and redirections, it requires tracing them for all available source files.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (maps in vector)
  [state options]
  (letfn [(f0 [%] (trace-imported-file state options %))]
         (vector/->items state f0)))
