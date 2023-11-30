(ns epictetus.uniform
  (:import  (org.lwjgl.opengl GL20 GL21 GL30 GL45)))

;; ::name
;; ::location
;; ::type ;; type of data in the data structure
;; ::data-structure ;; buffering, length of data
;; ::callback-fn ;; function called on rendering to set uniform value in the shader
;;
;; #::{:name  "view"
;;     :value :float ;;
;;     :type  :vec3}
;;


(defmacro method->fn
  [meth arity]
  (let [args (take arity (map symbol [:a :b :c :d
                                      :e :f :g :h
                                      :i :j :k :l]))
        signature (vec args)
        body (conj args meth)]
  `(fn ~signature
     ~body)))

((method->fn GL20/glUniform1f 2) 2 1)

{: (method->fn GL20/glUniform1f 2)
 : (method->fn GL20/glUniform1fv 3)
: (method->fn GL20/glUniform1i 2)
: (method->fn GL20/glUniform1iv 3)
: (method->fn GL30/glUniform1ui 2)
: (method->fn GL30/glUniform1uiv 3)
: (method->fn GL20/glUniform2f 3)
: (method->fn GL20/glUniform2fv 3)
: (method->fn GL20/glUniform2i 3)
: (method->fn GL20/glUniform2iv 3)
: (method->fn GL30/glUniform2ui 3)
: (method->fn GL30/glUniform2uiv 3)
: (method->fn GL20/glUniform3f 4)
: (method->fn GL20/glUniform3fv 3)
: (method->fn GL20/glUniform3i 4)
: (method->fn GL20/glUniform3iv 3)
: (method->fn GL30/glUniform3ui 4)
: (method->fn GL30/glUniform3uiv 3)
: (method->fn GL20/glUniform4f 5)
: (method->fn GL20/glUniform4fv 3)
: (method->fn GL20/glUniform4i 5)
: (method->fn GL20/glUniform4iv 3)
: (method->fn GL30/glUniform4ui 5)
: (method->fn GL30/glUniform4uiv 3)
: (method->fn GL20/glUniformMatrix2fv 4)
: (method->fn GL21/glUniformMatrix2x3fv 4)
: (method->fn GL21/glUniformMatrix2x4fv 4)
: (method->fn GL20/glUniformMatrix3fv 4)
: (method->fn GL21/glUniformMatrix3x2fv 4)
: (method->fn GL21/glUniformMatrix3x4fv 4)
: (method->fn GL20/glUniformMatrix4fv 4)
: (method->fn GL21/glUniformMatrix4x2fv 4)
: (method->fn GL21/glUniformMatrix4x3fv 4)
