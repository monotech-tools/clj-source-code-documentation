
(ns source-code-documentation.trace.utils
    (:require [fruits.vector.api :as vector]
              [fruits.regex.api :as regex]
              [fruits.string.api :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn def-value-symbol
  ; @ignore
  ;
  ; @description
  ; Returns the value of a specific def declaration in case its type is symbol (symbol type def declaration values are imported).
  ;
  ; @param (map) file-data
  ; @param (map) header
  ;
  ; @example
  ; (def-value-symbol {:ns-map {:defs [{:name "my-function" :value {:symbol "another-namespace/another-function"}}]}})
  ;                   {:name "my-function" :blocks [...]})
  ; =>
  ; "another-namespace/another-function"
  ;
  ; @return (string)
  [file-data {:keys [name]}]
  (letfn [(f0 [%] (-> % :name (= name)))]
         (-> file-data :ns-map :defs (vector/first-match f0) :value :symbol)))

(defn def-value-symbol-namespace
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (map) header
  ;
  ; @example
  ; (def-value-symbol-namespace {:ns-map {:defs [{:name "my-function" :value {:symbol "another-namespace/another-function"}}]}})
  ;                             {:name "my-function" :blocks [...]})
  ; =>
  ; "another-namespace"
  ;
  ; @return (string)
  [file-data header]
  (-> file-data (def-value-symbol header)
                (string/before-first-occurence "/" {:return? false})
                (string/to-nil {:if-empty? true})))

(defn def-value-symbol-name
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (map) header
  ;
  ; @example
  ; (def-value-symbol-name {:ns-map {:defs [{:name "my-function" :value {:symbol "another-namespace/another-function"}}]}})
  ;                        {:name "my-function" :blocks [...]})
  ; =>
  ; "another-function"
  ;
  ; @return (string)
  [file-data header]
  (-> file-data (def-value-symbol header)
                (string/after-last-occurence "/" {:return? true})
                (string/to-nil {:if-empty? true})))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn pointer-namespace
  ; @ignore
  ;
  ; @description
  ; Returns the namespace of the given link / redirection pointer.
  ;
  ; @param (map) file-data
  ; @param (map) header
  ; @param (string) pointer
  ;
  ; @example
  ; (pointer-namespace {:headers [{:name "my-function" :blocks [{:type :redirect :meta ["another-namespace/another-function"]}]}]}
  ;                    {:name "my-function" :blocks [...]}
  ;                    "another-namespace/another-function")
  ; =>
  ; "another-namespace"
  ;
  ; @example
  ; (pointer-namespace {:headers [{:name "my-function" :blocks [{:type :redirect :meta ["another-namespace/another-function"]}]}]}
  ;                    {:name "my-function" :blocks [...]}
  ;                    "another-function")
  ; =>
  ; nil
  ;
  ; @return (string)
  [_ _ pointer]
  (-> pointer (string/before-first-occurence "/" {:return? false})
              (string/to-nil {:if-empty? true})))

(defn pointer-name
  ; @ignore
  ;
  ; @description
  ; Returns the name of the given link / redirection pointer.
  ;
  ; @param (map) file-data
  ; @param (map) header
  ; @param (string) pointer
  ;
  ; @example
  ; (pointer-name {...}
  ;               {:name "my-function" :blocks [...]}
  ;               "another-namespace/another-function")
  ; =>
  ; "another-function"
  ;
  ; @example
  ; (pointer-name {...}
  ;               {:name "my-function" :blocks [...]}
  ;               "another-function")
  ; =>
  ; "another-function"
  ;
  ; @example
  ; (pointer-name {...}
  ;               {:name "my-function" :blocks [...]}
  ;               "")
  ; =>
  ; nil
  ;
  ; @return (string)
  [_ _ pointer]
  (-> pointer (string/after-last-occurence "/" {:return? true})
              (string/to-nil {:if-empty? true})))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn derive-pointer-namespace
  ; @ignore
  ;
  ; @description
  ; - Derives the namespace from the given link / redirection pointer.
  ; - Uses the namespace from the declaration's symbol type value (if any, and the declaration is a def) as a fallback.
  ; - Uses the file namespace as a second fallback.
  ; - Replaces the '*' wildcard character with the file namespace.
  ;
  ; @param (map) file-data
  ; @param (map) header
  ; @param (string) pointer
  ;
  ; @example
  ; (derive-pointer-namespace {...}
  ;                           {:name "my-function" :blocks [...]}
  ;                           "another-namespace/another-function")
  ; =>
  ; "another-namespace"
  ;
  ; @return (string)
  [file-data header pointer]
  (let [file-namespace (-> file-data :ns-map :declaration :name)]
       (-> (or (pointer-namespace          file-data header pointer)
               (def-value-symbol-namespace file-data header)
               (-> file-namespace))
           (regex/replace-match #"^\*$" file-namespace))))

(defn derive-pointer-name
  ; @ignore
  ;
  ; @description
  ; - Derives the name from the given link / redirection pointer.
  ; - Uses the name from the declaration's symbol type value (if any, and the declaration is a def) as a fallback.
  ; - Uses the declaration name as a second fallback.
  ; - Replaces the '*' wildcard character with the declaration name.
  ;
  ; @param (map) file-data
  ; @param (map) header
  ; @param (string) pointer
  ;
  ; @example
  ; (derive-pointer-name {...}
  ;                      {:name "my-function" :blocks [...]}
  ;                      "another-namespace/another-function")
  ; =>
  ; "another-function"
  ;
  ; @return (string)
  [file-data {:keys [name] :as header} pointer]
  (-> (or (pointer-name          file-data header pointer)
          (def-value-symbol-name file-data header)
          (-> name))
      (regex/replace-match #"^\*$" name)))

(defn derive-pointer
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (map) header
  ; @param (map) header-block
  ;
  ; @example
  ; (derive-pointer {...}
  ;                 {:name "my-function" :blocks [...]}
  ;                 {:type :redirect :meta "..."})
  ; =>
  ; :another-namespace/another-function
  ;
  ; @return (namespaced keyword)
  [file-data header header-block]
  (let [pointer (-> header-block :meta first)]
       (keyword (derive-pointer-namespace file-data header pointer)
                (derive-pointer-name      file-data header pointer))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn invoke-pointer-namespace-alias
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (map) header
  ; @param (namespaced pointer) pointer
  ;
  ; @usage
  ; (invoke-pointer-namespace-alias {...}
  ;                                 {:name "my-function" :blocks [...]}
  ;                                 :another-namespace/another-function)
  ;
  ; @return (namespaced keyword)
  [file-data _ pointer]
  (let [pointer-namespace (namespace pointer)
        pointer-name      (name      pointer)]
       (letfn [(f0 [%] (if (-> % :alias (= pointer-namespace))
                           (-> % :name)))]
              (if-let [pointer-namespace (->> file-data :ns-map :declaration :require :deps (some f0))]
                      (keyword pointer-namespace pointer-name)
                      (-> pointer)))))
