
(ns source-code-documentation.print.utils
    (:require [fruits.string.api                        :as string]
              [io.api                                   :as io]
              [source-code-documentation.assemble.utils :as assemble.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn namespace-path
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (string) extension
  ;
  ; @return (string)
  [_ _ file-data extension]
  (-> file-data :ns-map :declaration :name (string/replace-part "." "/")
                                           (string/prepend "/" extension "/")
                                           (string/append ".html")))

(defn page-print-path
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (string)
  [state {:keys [output-path] :as options} file-data]
  (let [extension      (-> file-data :filepath io/filepath->extension)
        namespace-path (namespace-path state options file-data extension)]
       (str output-path namespace-path)))

(defn cover-print-path
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (string)
  [state {:keys [output-path] :as options}]
  (str output-path "/index.html"))
