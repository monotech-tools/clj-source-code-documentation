
(ns source-code-documentation.api
    (:require [source-code-documentation.core.engine :as core.engine]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial How to place and format documentation content in Clojure files?
;
; Tutorials:
;
; @code
; (ns my-namespace)
;
; ; @tutorial My tutorial
; ;
; ; Lorem ipsum dolor sit amet.
; ;
; ; @usage
; ; (my-function ...)
; @---
;
; Constant declaration headers:
;
; @code
; ; @description
; ; Lorem ipsum dolor sit amet.
; ;
; ; @constant (string)
; (def MY-CONSTANT "...")
;
; Function declaration headers:
;
; @code
; (defn my-function
;   ; @description
;   ; Lorem ipsum dolor sit amet.
;   ;
;   ; @param (integer) a
;   ; @param (integer)(opt) b
;   ;
;   ; @return (integer)
;   [a & [b]] ...)

; @tutorial Markers
;
; @code
; ; @atom
; ; @bug
; ; @code
; ; @constant
; ; @description
; ; @ignore
; ; @important
; ; @info
; ; @link
; ; @note
; ; @param
; ; @preview
; ; @redirect
; ; @return
; ; @tutorial
; ; @usage
; ; @---
;
; Atom marker:
;
; @code
; ; @atom (map)
; (def MY-ATOM (atom {...}))
;
; Bug marker:
;
; @code
; (defn my-function
;   ; @bug (#0069)
;   ; Lorem ipsum dolor sit amet.
;   ;
;   ; @return (string)
;   [] ...)
;
; Code marker:
;
; @code
; ; @tutorial My tutorial
; ;
; ; Lorem ipsum dolor sit amet.
; ;
; ; @code
; ; (my-function "...")
;
; Constant marker:
;
; @code
; ; @constant (map)
; (def MY-CONSTANT {...})
;
; Description marker:
;
; @code
; (defn my-function
;   ; @description
;   ; Lorem ipsum dolor sit amet.
;   ;
;   ; @return (integer)
;   [] ...)
;
; Ignore marker:
;
; @code
; (defn my-function
;   ; @description
;   ; Lorem ipsum dolor sit amet.
;   ;
;   ; @return (integer)
;   ;
;   ; @ignore
;   ; Content below the @ignore marker will not be in the documentation.
;   [] ...)
;
; Important marker:
;
; @code
; (defn my-function
;   ; @important
;   ; Lorem ipsum dolor sit amet.
;   ;
;   ; @return (integer)
;   [] ...)
;
; Info marker:
;
; @code
; (defn my-function
;   ; @info
;   ; Lorem ipsum dolor sit amet.
;   ;
;   ; @return (integer)
;   [] ...)
;
; Link marker:
;
; @code
; (defn my-function
;   ; @description
;   ; Lorem ipsum dolor sit amet.
;   ;
;   ; @link (another-function)
;   [] ...)
;
; (defn another-function
;  ; @note
;  ; Documentation of the 'another-function' function will be INSERTED into the documentation of the 'my-function' function.
;  ;
;  ; @return (integer)
;  [] ...)
; @---
;
; @code
; (ns my-namespace
;   (:require [another-namespace]))
;
; ; @description
; ; Lorem ipsum dolor sit amet.
; ;
; ; @link (another-namespace/another-function)
; (def my-function another-namespace/another-function)
; @---
;
; @code
; (ns my-namespace
;   (:require [another-namespace]))
;
; ; @description
; ; Lorem ipsum dolor sit amet.
; ;
; ; @link (another-namespace/*)
; (def my-function another-namespace/another-function)
; @---
;
; Note marker:
;
; @code
; (defn my-function
;   ; @note
;   ; Lorem ipsum dolor sit amet.
;   ;
;   ; @return (integer)
;   [] ...)
;
; Param marker:
;
; @code
; (defn my-function
;   ; @param (string) a
;   ; @param (string)(opt) b
;   [a & [b]] ...)
;
; @code
; (defn my-function
;   ; @param (string)(req) a
;   ; @param (string)(opt) b
;   [a & [b]] ...)
;
; @code
; (defn my-function
;   ; @param (map) a
;   ; {:foo (keywords in vector)
;   ;   [:bar, :baz, :boo]
;   ;   Default: [:bar]
;   ; @param (string) b}
;   [a b] ...)
;
; Preview marker:
;
; @code
; (defn my-function
;   ; @preview (relative-to-the-provided-previews-uri/my-image.png)
;   ;
;   ; @return (integer)
;   [] ...)
;
; Redirect marker:
;
; @code
; (defn my-function
;   ; @redirect (another-function)
;   [] ...)
;
; (defn another-function
;  ; @note
;  ; Documentation of the 'another-function' function will REPLACE the documentation of the 'my-function' function.
;  ;
;  ; @return (integer)
;  [] ...)
; @---
;
; @code
; (ns my-namespace
;   (:require [another-namespace]))
;
; ; @redirect (another-namespace/another-function)
; (def my-function another-namespace/another-function)
; @---
;
; @code
; (ns my-namespace
;   (:require [another-namespace]))
;
; ; @redirect (another-namespace/*)
; (def my-function another-namespace/another-function)
; @---
;
; Return marker:
;
; @code
; (defn my-function
;   ; @return (strings in vector)
;   [] ...)
;
; @code
; (defn my-function
;   ; @return (map)
;   ; {:foo (integer)
;   ;  :bar (integer)}
;   [] ...)
;
; Tutorial marker:
;
; @code
; ; @tutorial My tutorial
; ;
; ; Lorem ipsum dolor sit amet.
; ;
; ; @usage
; ; (my-function "...")
;
; Usage marker:
;
; @code
; (defn my-function
;   ; @param (integer) a
;   ; @param (integer)(opt) b
;   ;
;   ; @usage
;   ; (my-function 42)
;   ;
;   ; @usage
;   ; (my-function 42 69)
;   ;
;   ; @usage
;   ; (my-function 42 69)
;   ; =>
;   ; 2898
;   ;
;   ; @return (integer)
;   [a & [b]] ...)
;
; --- (block end) marker:
;
; @code
; ; @tutorial My tutorial
; ;
; ; Lorem ipsum dolor sit amet.
; ;
; ; @code
; ;
; ; By default, empty rows always close opened blocks, except if a block end marker
; ; tells the interpreter where is the actual end of the block.
; ;
; ; @---

; @tutorial Links
;
; @code
; (defn my-function
;   ; @description
;   ; Place a [link](https://...) or an [anchor](#another-function) anywhere in your documentation content.
;   ;
;   ; @return (integer)
;   [] ...)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @redirect (source-code-documentation.core.engine/*)
(def generate-documentation! core.engine/generate-documentation!)
