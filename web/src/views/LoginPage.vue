<template>
  <main class="login">
    <section class="intro">
      <div class="h-100">
        <div
          class="mask d-flex align-items-center h-100"
          style="background-color: #f3f2f2"
        >
          <div class="container">
            <div class="row d-flex justify-content-center align-items-center">
              <div class="col-12 col-lg-9 col-xl-8">
                <div class="card" style="border-radius: 1rem">
                  <div class="row g-0">
                    <div class="col-md-4 d-none d-md-block">
                      <img
                        src="@/assets/images/webma-login-banner.png"
                        alt="login form"
                        class="img-fluid"
                        style="
                          border-top-left-radius: 1rem;
                          border-bottom-left-radius: 1rem;
                        "
                      />
                    </div>
                    <div class="col-md-8 d-flex align-items-center">
                      <div class="card-body py-5 px-14 p-md-5">
                        <form novalidate @submit.prevent="login">
                          <h4 class="fw-bold mb-6" style="color: #31465e">
                            Welcome Admin
                          </h4>
                          <p class="mb-6" style="color: #45526e">
                            To log in, please fill in these text fields with
                            your e-mail address and password.
                          </p>

                                                    <div class="mb-4">
                                                        <input type="email" id="forminput1" class="form-control mb-1"
                                                            placeholder="Email" v-model="login_form.email"
                                                            @blur="v$.email.$touch" />
                                                        <div v-for="error in v$.email.$errors" :key="error.$uid"
                                                            class="alert alert-danger">
                                                            <em> {{ error.$message }}</em>
                                                        </div>
                                                    </div>


                                                    <div class="mb-6">
                                                        <input type="password" id="forminput2" class="form-control mb-1"
                                                            placeholder="Password" v-model="login_form.password"
                                                            @blur="v$.password.$touch" />
                                                        <div v-for="error in v$.password.$errors" :key="error.$uid"
                                                            class="alert alert-danger">
                                                            <em> {{ error.$message }}</em>
                                                        </div>


                                                    </div>
                                                    <div class="d-flex justify-content-end pt-1 mb-4">
                                                        <button class="btn btn-primary btn-rounded text-white" type="submit"
                                                            style="background-color: #31465E;">Log in</button>
                                                    </div>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </main>
</template>

<script>
import { reactive } from "vue";
import { useStore } from "vuex";
import useVuelidate from "@vuelidate/core";
import { required, minLength, email, maxLength } from "@vuelidate/validators";

export default {
  setup() {
    const store = useStore();

    const login_form = reactive({
      email: "",
      password: "",
    });

    const rules = reactive({
      email: {
        required,
        email,
        maxLength: maxLength(254),
      },
      password: {
        required,
        minLength: minLength(6),
      },
    });

    const v$ = useVuelidate(rules, login_form);

    const login = async () => {
      const result = await v$.value.$validate();

      if (result) {
        v$.value.$reset();
        store.dispatch("auth/login", login_form);
      } else {
        alert("Please fill up the form correctly");
      }
    };

    return {
      login_form,
      login,
      v$,
    };
  },
};
</script>
