<template>
  <NavBar v-if="$route.meta.requiresAuth" />
  <main>
    <div
      style="padding-top: 175px; padding-bottom: 175px"
      v-if="$route.path !== '/'"
    >
      <router-view />
    </div>
    <div v-else>
      <router-view />
    </div>
  </main>
</template>

<script>
import { onBeforeMount } from "vue";
import { useStore } from "vuex";
import NavBar from "@/components/NavBar.vue";

export default {
  components: {
    NavBar,
  },
  setup() {
    onBeforeMount(async () => {
      await useStore().dispatch("auth/fetchUser");
    });
  },
};
</script>

<style>
#app {
  font-family: Roboto, Helvetica, Arial, sans-serif;
}
</style>
