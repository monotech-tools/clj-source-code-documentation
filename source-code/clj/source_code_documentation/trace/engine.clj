
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
  ; @param (map) snippet
  ;
  ; @usage
  ; (trace-redirection [...] {...} {...}
  ;                    {:name "my-function" :type :defn :content [{:marker :redirect :meta ["..."] :indent 1 ...} ...] ...}
  ;                    {:marker :redirect :meta ["..."] :indent 1 ...})
  ; =>
  ; {:marker :redirect :meta ["..."] :indent 1 :pointer "another-namespace/another-function"}
  ;
  ; @return (map)
  [_ _ file-data section snippet]
  (let [pointer (trace.utils/derive-pointer                 file-data section snippet)
        pointer (trace.utils/invoke-pointer-namespace-alias file-data section pointer)]
       (assoc snippet :pointer pointer)))

(defn trace-link
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) section
  ; @param (map) snippet
  ;
  ; @return (map)
  [state options file-data section snippet]
  ; Tracing links is the same process as tracing redirections.
  (trace-redirection state options file-data section snippet))

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
  ;                {:name "my-function" :type :defn :content [{:marker :redirect :meta ["..."] :indent 1 ...} ...] ...})
  ; =>
  ; {:name    "my-function"
  ;  :content [{:marker :redirect :meta ["..."] :indent 1 :pointer "another-namespace/another-function" ...} ...] ...}
  ;
  ; @return (map)
  [state options file-data section]
  (letfn [(f0 [%] (-> % :marker (= :redirect)))
          (f1 [%] (-> % :marker (= :link)))
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
  ; Traces the links and redirections in documentation contents of defs and defns creating pointers.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; {:trace-redirections? (boolean)(opt)
  ;  ...}
  ;
  ; @return (maps in vector)
  [state {:keys [trace-redirections?] :as options}]
  (letfn [(f0 [%] (trace-imported-file state options %))]
         (if trace-redirections? (-> state (vector/->items f0))
                                 (-> state))))
