import { createRouter, createWebHistory, START_LOCATION } from "vue-router";
import store from "@/store";

import Login from "@/views/LoginPage.vue";
import Dashboard from "@/views/DashboardPage.vue";
import Orders from "@/views/OrdersPage.vue";
import Products from "@/views/ProductsPage.vue";
import NotFound from "@/views/NotFoundPage.vue";

const routes = [
  {
    path: "/",
    name: "Login",
    component: Login,
    meta: {
      requiresAuth: false,
    },
  },
  {
    path: "/dashboard",
    name: "Dashboard",
    component: Dashboard,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: "/products",
    name: "Products",
    component: Products,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: "/orders",
    name: "Orders",
    component: Orders,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: "/:pathMatch(.*)*",
    name: "TFYO | Page Not Found",
    component: NotFound,
    meta: {
      requiresAuth: false,
    },
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

router.beforeEach(async (to, from, next) => {
  document.title = to.name;

  let currentUser;

  // If reloaded, get current user again
  if (from === START_LOCATION) {
    await store.dispatch("auth/fetchUser").then(() => {
      currentUser = store.getters["auth/user"];
    });
  } else {
    // Else, rely on store
    currentUser = store.getters["auth/user"];
  }

  if (currentUser === null) {
    if (to.meta.requiresAuth) {
      next("/");
    } else {
      next();
    }
  } else {
    if (to.path === "/") {
      next("/dashboard");
    } else {
      next();
    }
  }
});

export default router;
