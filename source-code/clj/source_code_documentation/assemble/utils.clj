
(ns source-code-documentation.assemble.utils
    (:require [fruits.hiccup.api :as hiccup]
              [fruits.string.api :as string]
              [fruits.regex.api :as regex]
              [fruits.vector.api :as vector]
              [io.api            :as io]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn symbol-anchor
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) declaration
  ;
  ; @return (string)
  [_ _ _ declaration]
  (-> declaration :name hiccup/value (string/prepend "#")))

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

(defn namespace-uri
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (string) extension
  ;
  ; @return (string)
  [state {:keys [base-uri] :as options} file-data extension]
  (let [namespace-path (namespace-path state options file-data extension)]
       (str base-uri namespace-path)))

(defn preview-image-uri
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) header-block
  ;
  ; @return (string)
  [_ {:keys [previews-uri]} header-block]
  (let [preview-path (-> header-block :meta first)]
       (str previews-uri "/" preview-path)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn filter-namespaces
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (string) extension
  ;
  ; @example
  ; (filter-namespaces [{:filepath "source-code/my_namespace_a.clj"  ...}
  ;                     {:filepath "source-code/my_namespace_a.cljs" ...}]
  ;                    {...}
  ;                    "clj")
  ; =>
  ; [{:filepath "source-code/my_namespace_a.clj" ...}]
  ;
  ; @return (maps in vector)
  [state _ extension]
  (letfn [(f0 [%] (-> % :filepath io/filepath->extension (= extension)))]
         (-> state (vector/keep-items-by f0)
                   (vector/to-nil {:if-empty? true}))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn parse-link
  ; @ignore
  ;
  ; @param (string) n
  ; @param (integer) position
  ;
  ; @usage
  ; (parse-link "My text [My link](#my-anchor)")
  ; =>
  ; ["My text" [:a {:href "#my-anchor"} "My text"]]
  ;
  ; @param (vector)
  [n position]
  (letfn [(f0 [%] (regex/re-first % #"(?<=\[)[^\[\]]{1,}(?=\]\()"))
          (f1 [%] (regex/re-first % #"(?<=\]\()[^\(\)]{1,}(?=\))"))]
         (let [link (-> n (string/keep-range position)
                          (string/to-first-occurence ")"))]
              [(string/keep-range n 0 position)
               [:a {:class [:inline-link :color--primary] :href (f1 link)} (f0 link)]
               (string/keep-range n (+ position (count link)))])))

(defn parse-links
  ; @ignore
  ;
  ; @param (vector) n
  ;
  ; @usage
  ; (parse-links ["My text [My anchor](#my-anchor)" [:br] ...])
  ; =>
  ; ["My text" [:a {:href "#my-anchor"} "My text"] [:br] ...]
  ;
  ; @param (vector)
  [n]
  (letfn [(f0 [result %]
              (if-let [position (regex/first-dex-of % #"\[[^\[\]]{1,}\]\([^\(\)]{1,}\)")]
                      (vector/concat-items result (parse-links (parse-link % position)))
                      (vector/conj-item    result %)))]
         (reduce f0 [] n)))

(defn unparse-entities
  ; @ignore
  ;
  ; @param (vector) n
  ;
  ; @usage
  ; (unparse-entities ["This is an HTML element: <img />" [:br] ...])
  ; =>
  ; ["This is an HTML element: &lt;img /&gt;" [:br] ...]
  ;
  ; @param (vector)
  [n]
  (letfn [(f0 [%]
              (if (-> % string?)
                  (-> % (string/replace-part "<" "&lt;")
                        (string/replace-part ">" "&gt;"))
                  (-> %)))]
         (vector/->items n f0)))
