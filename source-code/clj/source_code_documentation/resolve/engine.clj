
(ns source-code-documentation.resolve.engine
    (:require [source-code-documentation.resolve.utils :as resolve.utils]
              [fruits.map.api :as map]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn resolve-header-redirection
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (string) def*-name
  ; @param (map) def*-header-block
  ;
  ; @return (map)
  [state _ file-data def*-name def*-header-block]
  (let [redirection-namespace (-> def*-header-block :pointer namespace)
        redirection-name      (-> def*-header-block :pointer name)]
       (letfn [(f0 [%] (-> % :ns-map :declaration :name (= redirection-namespace)))]
              (if-let [target-file-data (vector/first-match state f0)]
                      (if-let [target-headers (-> target-file-data :headers (get redirection-name))]
                              (do
                                (println (-> file-data :ns-map :declaration :name))
                                (println def*-name)
                                (println target-headers)
                                (println))))))
              ;(println redirection-namespace redirection-name)))

  def*-header-block)

(defn resolve-header-redirections
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (string) def*-name
  ; @param (maps in vector) def*-header
  ;
  ; @example
  ; (resolve-header-redirections [...] {...} {...}
  ;                              "my-function"
  ;                              [{:type :redirect :meta ["..."] :indent 1}])
  ; =>
  ; [{:type :redirect :meta ["..."] :indent 1}]
  ;
  ; @return (maps in vector)
  [state options file-data def*-name def*-header]
  (letfn [(f0 [%] (-> % :type (= :redirect)))
          (f1 [%] (resolve-header-redirection state options file-data def*-name %))]
         (vector/->items-by def*-header f0 f1)))

(defn resolve-imported-file
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (maps in vector)
  [state options {:keys [headers] :as file-data}]
  (letfn [(f0 [def*-name def*-header]
              (resolve-header-redirections state options file-data def*-name def*-header))]
         (update file-data :headers map/->values f0 {:provide-key? true})))

(defn resolve-imported-files
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (maps in vector)
  [state options]
  (letfn [(f0 [file-data] (resolve-imported-file state options file-data))]
         (vector/->items state f0)))
