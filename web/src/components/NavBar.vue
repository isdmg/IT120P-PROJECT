<template>
    <header>
        <div class="container-fluid">
            <!-- Navbar -->
            <nav class="navbar navbar-expand-lg fixed-top bg-light navbar-light py-3">
                <div class="container">
                    <a class="navbar-brand">
                        <img style="position: absolute; z-index: 1; margin-top: 74px; height: 130px;" id="MDB-logo" src="@/assets/webma-logo.png" alt="MDB Logo"
                        draggable="false"/>
                    </a>
                    <button class="navbar-toggler" type="button" data-mdb-toggle="collapse"
                        data-mdb-target="#navbarSupportedContent" aria-controls="navbarSupportedContent"
                        aria-expanded="false" aria-label="Toggle navigation">
                        <i class="fas fa-bars"></i>
                    </button>
                    <div class="collapse navbar-collapse" id="navbarSupportedContent">
                        <ul class="navbar-nav ms-auto align-items-center">
                            <li class="nav-item">
                                <router-link :to="`/dashboard`" class="nav-link mx-2"><i class="fa-solid fa-house pe-2"></i>Home</router-link>
                            </li>
                            <li class="nav-item">
                                <router-link :to="`/products`" class="nav-link mx-2"><i class="fa-solid fa-box pe-2"></i>Products</router-link>
                            </li>
                            <li class="nav-item">
                                <router-link :to="`/orders`" class="nav-link mx-2"><i class="fa-solid fa-cart-shopping pe-2"></i>Orders</router-link>
                            </li>
                            <li class="nav-item ms-3">
                                <a class="btn btn-purple btn-danger btn-rounded text-light" @click="logout()">Log out</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </nav>
        </div>
    </header>
</template>

<script>
import store from "@/store";

export default {
  methods: {
    logout() {
      store.dispatch("auth/logout");
    },
  },
};
</script>

<style scoped src="@/assets/css/navbar.css"></style>
