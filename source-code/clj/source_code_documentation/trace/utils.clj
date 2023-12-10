
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
  ; @param (string) declaration-name
  ;
  ; @example
  ; (def-value-symbol {:ns-map {:defs [{:name "my-function" :value {:symbol "another-namespace/another-function"}}]}})
  ;                   "my-function")
  ; =>
  ; "another-namespace/another-function"
  ;
  ; @return (string)
  [file-data declaration-name]
  (letfn [(f0 [%] (-> % :name (= declaration-name)))]
         (-> file-data :ns-map :defs (vector/first-match f0) :value :symbol)))

(defn def-value-symbol-namespace
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (string) declaration-name
  ;
  ; @example
  ; (def-value-symbol-namespace {:ns-map {:defs [{:name "my-function" :value {:symbol "another-namespace/another-function"}}]}})
  ;                             "my-function")
  ; =>
  ; "another-namespace"
  ;
  ; @return (string)
  [file-data declaration-name]
  (-> file-data (def-value-symbol declaration-name)
                (string/before-first-occurence "/" {:return? false})
                (string/use-nil)))

(defn def-value-symbol-name
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (string) declaration-name
  ;
  ; @example
  ; (def-value-symbol-name {:ns-map {:defs [{:name "my-function" :value {:symbol "another-namespace/another-function"}}]}})
  ;                        "my-function")
  ; =>
  ; "another-function"
  ;
  ; @return (string)
  [file-data declaration-name]
  (-> file-data (def-value-symbol declaration-name)
                (string/after-last-occurence "/" {:return? true})
                (string/use-nil)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn pointer-namespace
  ; @ignore
  ;
  ; @description
  ; Returns the namespace of the given redirection pointer.
  ;
  ; @param (map) file-data
  ; @param (string) declaration-name
  ; @param (string) pointer
  ;
  ; @example
  ; (pointer-namespace {:headers {"my-function" [{:type :redirect :meta ["another-namespace/another-function"]}]}}
  ;                    "my-function"
  ;                    "another-namespace/another-function")
  ; =>
  ; "another-namespace"
  ;
  ; @example
  ; (pointer-namespace {:headers {"my-function" [{:type :redirect :meta ["another-namespace/another-function"]}]}}
  ;                    "my-function"
  ;                    "another-function")
  ; =>
  ; nil
  ;
  ; @return (string)
  [_ _ pointer]
  (-> pointer (string/before-first-occurence "/" {:return? false})
              (string/use-nil)))

(defn pointer-name
  ; @ignore
  ;
  ; @description
  ; Returns the name of the given redirection pointer.
  ;
  ; @param (map) file-data
  ; @param (string) declaration-name
  ; @param (string) pointer
  ;
  ; @example
  ; (pointer-name {...}
  ;               "my-function"
  ;               "another-namespace/another-function")
  ; =>
  ; "another-function"
  ;
  ; @example
  ; (pointer-name {...}
  ;               "my-function"
  ;               "another-function")
  ; =>
  ; "another-function"
  ;
  ; @example
  ; (pointer-name {...}
  ;               "my-function"
  ;               "")
  ; =>
  ; nil
  ;
  ; @return (string)
  [_ _ pointer]
  (-> pointer (string/after-last-occurence "/" {:return? true})
              (string/use-nil)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn derive-pointer-namespace
  ; @ignore
  ;
  ; @description
  ; - Derives the namespace from the given redirection pointer.
  ; - Uses the namespace from the declaration's symbol type value (if any, and the declaration is a def) as a fallback.
  ; - Uses the file namespace as a second fallback.
  ; - Replaces the '*' wildcard character with the file namespace.
  ;
  ; @param (map) file-data
  ; @param (string) declaration-name
  ; @param (string) pointer
  ;
  ; @example
  ; (derive-pointer-namespace {...}
  ;                           "my-function"
  ;                           "another-namespace/another-function")
  ; =>
  ; "another-namespace"
  ;
  ; @return (string)
  [file-data declaration-name pointer]
  (let [file-namespace (-> file-data :ns-map :declaration :name)]
       (-> (or (pointer-namespace          file-data declaration-name pointer)
               (def-value-symbol-namespace file-data declaration-name)
               (-> file-namespace))
           (regex/replace-match #"^\*$" file-namespace))))

(defn derive-pointer-name
  ; @ignore
  ;
  ; @description
  ; - Derives the name from the given redirection pointer.
  ; - Uses the name from the declaration's symbol type value (if any, and the declaration is a def) as a fallback.
  ; - Uses the declaration name as a second fallback.
  ; - Replaces the '*' wildcard character with the declaration name.
  ;
  ; @param (map) file-data
  ; @param (string) declaration-name
  ; @param (string) pointer
  ;
  ; @example
  ; (derive-pointer-name {...}
  ;                      "my-function"
  ;                      "another-namespace/another-function")
  ; =>
  ; "another-function"
  ;
  ; @return (string)
  [file-data declaration-name pointer]
  (-> (or (pointer-name          file-data declaration-name pointer)
          (def-value-symbol-name file-data declaration-name)
          (-> declaration-name))
      (regex/replace-match #"^\*$" declaration-name)))

(defn derive-pointer
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (string) declaration-name
  ; @param (string) pointer
  ;
  ; @example
  ; (derive-pointer {...}
  ;                 "my-function"
  ;                 "another-namespace/another-function")
  ; =>
  ; :another-namespace/another-function
  ;
  ; @return (namespaced keyword)
  [file-data declaration-name declaration-header-block]
  (let [pointer (-> declaration-header-block :meta first)]
       (keyword (derive-pointer-namespace file-data declaration-name pointer)
                (derive-pointer-name      file-data declaration-name pointer))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn invoke-pointer-namespace-alias
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (string) declaration-name
  ; @param (namespaced pointer) pointer
  ;
  ; @usage
  ; (invoke-pointer-namespace-alias {...}
  ;                                 "my-function"
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
