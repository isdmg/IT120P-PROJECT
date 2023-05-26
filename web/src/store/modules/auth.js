import router from "@/router";
import { auth, db } from "@/firebase";
import { signInWithEmailAndPassword, signOut } from "firebase/auth";
import { doc, getDoc } from "firebase/firestore";

export default {
  namespaced: true,
  state: {
    user: null,
  },
  mutations: {
    SET_USER(state, user) {
      state.user = user;
    },
    CLEAR_USER(state) {
      state.user = null;
    },
  },
  actions: {
    async login({ commit }, details) {
      const { email, password } = details;

      try {
        await signInWithEmailAndPassword(auth, email, password);
      } catch (error) {
        switch (error.code) {
          case "auth/user-not-found":
            alert("User not found");
            break;
          case "auth/wrong-password":
            alert("Wrong password");
            break;
          default:
            alert("Something went wrong");
        }
        return;
      }

      const userRef = doc(db, "Users", auth.currentUser.uid);
      const userSnap = await getDoc(userRef);

      if (!userSnap.exists()) {
        alert("User not found");
        return;
      } else {
        const user = userSnap.data();

        console.log(user.isAdmin);
        if (user.isAdmin === false) {
          alert("The user is not an admin");
          return;
        }
      }

      commit("SET_USER", auth.currentUser);
      router.push("/dashboard");
    },
    async logout({ commit }) {
      await signOut(auth);

      commit("CLEAR_USER");
      router.push("/");
    },
    async fetchUser({ commit }) {
      return new Promise((resolve) => {
        auth.onAuthStateChanged(async (user) => {
          if (user === null) {
            commit("CLEAR_USER");
          } else {
            const userRef = doc(db, "Users", auth.currentUser.uid);
            const userSnap = await getDoc(userRef);

            const user = userSnap.data();
            console.log(user.isAdmin);

            if (user.isAdmin === false) {
              await signOut(auth);
              commit("CLEAR_USER");
              router.push("/");
              return;
            }

            commit("SET_USER", user);
          }
          resolve();
        });
      });
    },
  },
  getters: {
    user: (state) => state.user,
  },
};
