(ns epictetus.entity
  (:require [epictetus.effect :refer [reg-fx]]
            [epictetus.state  :as state]))

(defn do-mut
  [entity mut]
  (let [[k value] mut]
    (if (vector? k)
      (assoc-in entity k value)
      (assoc    entity k value))))

(reg-fx :entity/set
        (fn mutate-entity! [[id muts]]
          ;; m is a map of entity ids and a map of data to set
          ;; (assoc fx :entity/set [:hero {:motion [:walk :right]}])
          (let [entity (get @state/entities id)]
            (->> muts
                 (reduce do-mut entity)
                 (swap! state/entities assoc id)))))














;;   #::{:program  :sprite
;;       :position [0.0 0.0 -50.0]
;;       :scale    0.15
;;       :anim/duration  1.12
;;       :anim/frames    4
;;       :anim/start     0.0
;;       :assets {}}
;;
;;
;;   (def entity
;;     #::{:id      nil
;;         :program nil
;;         :assets  nil})
;;
;;
;;   (defn reg-system
;;     ([entity anim]
;;      (when (anim-support? entity anim)
;;       (assoc entity :anim/sprite anim))))
;;
;;
;;   (defn textures
;;     [entity]
;;     (get-in entity [:assets :textures]))
;;
;;
;;
;;
;;     ;; Module definition
;;     {:id        :sprite-animation
;;
;;      ;; Vector of compatible progam ids
;;      :programs  [:sprite]
;;
;;      ;; Extra textures to load
;;      ;; TODO Is it wise to treat textures separatly
;;      ;; although they are regular uniforms ?
;;      :textures  {:textIndex0 "textures/hero.png"}
;;
;;      :uniforms  {:animIndex (fn [])}
;;      :entity     {:frames   4
;;                  :duration 1.0
;;                  :start    nil}}
