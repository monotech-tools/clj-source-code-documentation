
(ns source-code-documentation.assemble.utils
    (:require [fruits.hiccup.api :as hiccup]
              [fruits.regex.api  :as regex]
              [fruits.string.api :as string]
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
                                           (string/prepend      "/" extension "/")
                                           (string/append       ".html")))

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
  ; @param (map) content-block
  ;
  ; @return (string)
  [_ {:keys [previews-uri]} content-block]
  (let [preview-path (-> content-block :meta first)]
       (str previews-uri "/" preview-path)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn filter-sections
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (keyword) type
  ;
  ; @usage
  ; (filter-sections [...] {...} {:sections [...]} :tutorial)
  ;
  ; @return (maps in vector)
  [state _ file-data type]
  (letfn [(f0 [%] (-> % :type (= type)))]
         (-> file-data :sections (vector/keep-items-by f0)
                                 (vector/to-nil {:if-empty? true}))))

(defn filter-namespaces
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (string) extension
  ;
  ; @usage
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

(defn sort-namespaces
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @usage
  ; (sort-namespaces [{:ns-map {:declaration {:name "my-namespace"}}      ...}
  ;                   {:ns-map {:declaration {:name "another-namespace"}} ...}]
  ;                  {...})
  ; =>
  ; [{:ns-map {:declaration {:name "another-namespace"}} ...}
  ;  {:ns-map {:declaration {:name "my-namespace"}}      ...}]
  ;
  ; @return (maps in vector)
  [state _]
  (letfn [(f0 [%] (-> % :ns-map :declaration :name))]
         (-> state (vector/sort-items-by f0))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn sort-declaration-content
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; {:declaration-order (keywords in vector)(opt)}
  ; @param (map) section
  ; {:content (maps in vector)
  ;  ...}
  ;
  ; @usage
  ; (sort-declaration-content [...] {...} {:content [...] :declaration-order [...] ...})
  ; =>
  ; {:content [...]
  ;  ...}
  ;
  ; @return (map)
  [_ {:keys [declaration-order]} section]
  (letfn [(f0 [%]   (vector/sort-items-by % f1 :type))
          (f1 [a b] (vector/order-comparator declaration-order a b))]
         (if declaration-order (-> section (update :content f0))
                               (-> section))))

(defn sort-tutorial-content
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; {:tutorial-order (keywords in vector)(opt)}
  ; @param (map) section
  ; {:content (maps in vector)
  ;  ...}
  ;
  ; @usage
  ; (sort-tutorial-content [...] {...} {:content [...] :tutorial-order [...] ...})
  ; =>
  ; {:content [...]
  ;  ...}
  ;
  ; @return (map)
  [_ {:keys [tutorial-order]} section]
  (letfn [(f0 [%]   (vector/sort-items-by % f1 :type))
          (f1 [a b] (vector/order-comparator tutorial-order a b))]
         (if tutorial-order (-> section (update :content f0))
                            (-> section))))

(defn append-declaration-source-code
  ; @ignore
  ;
  ; @description
  ; Appends the imported source code of declaration as a content block of the given declaration.
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) section
  ; {:content (maps in vector)
  ;  :source-code (string)
  ;  ...}
  ;
  ; @usage
  ; (append-declaration-source-code [...] {...} {:content [...] :source-code "(defn ...)" :type :defn ...})
  ; =>
  ; {:content     [{:type :source-code :text ["(defn ...)"]} ...]
  ;  :source-code "..."
  ;  :type        :defn
  ;  ...}
  ;
  ; @return (map)
  [_ _ {:keys [source-code] :as section}]
  (let [content-block {:type :source-code :text [source-code]}]
       (-> section (update :content vector/cons-item content-block))))

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
  (letfn [(f0 [%] (regex/re-first % #"(?<=\[)[^\[\]]+(?=\]\()"))
          (f1 [%] (regex/re-first % #"(?<=\]\()[^\(\)]+(?=\))"))]
         (let [link (-> n (string/keep-range position)
                          (string/to-first-occurence ")"))]
              [(string/keep-range n 0 position)
               [:a {:class [:inline-link :color--hard-blue] :href (f1 link)} (-> link f0 string/trim)]
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
              (if-let [position (regex/first-dex-of % #"\[[^\[\]]+\]\([^\(\)]+\)")]
                      (vector/concat-items result (parse-links (parse-link % position)))
                      (vector/conj-item    result %)))]
         (reduce f0 [] n)))

(defn unparse-html
  ; @ignore
  ;
  ; @param (vector) n
  ;
  ; @usage
  ; (unparse-html ["This is an HTML element: <img />" [:br] ...])
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

(defn gap-rows
  ; @ignore
  ;
  ; @param (vector) n
  ;
  ; @usage
  ; (gap-rows ["Row #1" "Row #2" ...])
  ; =>
  ; ["Row #1" [:br] "Row #2" [:br] ...]
  ;
  ; @param (vector)
  [n]
  (vector/gap-items n [:br]))
