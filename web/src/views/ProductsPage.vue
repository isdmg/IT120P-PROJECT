<template>
  <v-overlay :model-value="loading" class="align-center justify-center" persistent>
    <v-progress-circular color="brown" indeterminate size="64"></v-progress-circular>
  </v-overlay>

  <v-overlay :model-value="loadingForm" class="align-center justify-center" persistent>
    <v-progress-circular color="brown" indeterminate size="64"></v-progress-circular>
  </v-overlay>

  <v-dialog v-model="editDialog" transition="dialog-top-transition" width="640" persistent>
    <template v-slot:activator="{ props }">
      <div class="wrapper">
        <div class="container-fluid d-flex justify-content-center align-items-start m-4">
          <div class="row">
            <div class="col-md-12">
              <div class="clearfix" style="height: 30px">
                <input class="float-md-start form-control search" placeholder="Search Product Name" type="text"
                  id="search" style="height: 100%; font-size: small" v-model="searchValue" />
                <!-- Add -->
                <v-dialog v-model="addDialog" transition="dialog-top-transition" width="640" persistent>
                  <template v-slot:activator="{ props }">
                    <button class="btn btn-primary float-md-end d-flex justify-content-center"
                      style="height: 100%; background-color: #5f323f;" v-bind="props" @click="addRow()">
                      <span class="fa-fw text-white fa-solid fa-plus"></span>
                    </button>
                  </template>
                  <template v-slot:default="{ isActive }">
                    <v-card>
                      <v-toolbar :color="'#2F437A'">
                        <v-toolbar-title class="text-white">Add Product</v-toolbar-title>
                        <v-toolbar-items>
                          <v-btn @click="isActive.value = false;
                          resetAddForm();
                                                      ">
                            <span class="fa-fw text-white fa-solid fa-xmark"></span>
                          </v-btn>
                        </v-toolbar-items>
                      </v-toolbar>
                      <v-card-text>
                        <!-- Set novalidate because of custom validations -->
                        <form novalidate>
                          <!-- No image uploaded -->
                          <div class="mb-3" v-if=" !uploadedProductPhotoUrl ">
                            <label class="form-label">Product Photo</label>
                            <label for="productPhoto" class="form-control btn btn-primary">Upload</label>
                            <input type="file" @change="
                              onFileChangeAdd($event);
                              previewPhotoUrl($event);
                            " class="form-control d-none" id="productPhoto" accept="image/*" />
                            <div v-for=" error  in  addForm$.imageRef.$errors " :key=" error.$uid " class="alert alert-danger">
                              <em> {{ error.$message }} </em>
                            </div>
                          </div>

                          <!-- Has image -->
                          <div class="mb-3" v-else>
                            <label class="form-label">Product Photo</label>
                            <!-- TODO: Don't use br -->
                            <br />
                            <a class="form-text" :href=" uploadedProductPhotoUrl " target="_blank">
                              {{ uploadedProductPhoto.name }}
                            </a>
                            <!-- Delete photo -->
                            <span type="button" class="fa-solid fa-xmark fa-fw" @click="
                              deleteProductPhotoUrl();
                              deleteFileAdd();
                            "></span>
                          </div>

                          <!-- Product Category -->
                          <div class="mb-3">
                            <label for="productCategory" class="form-label">Category</label>
                            <select class="form-control" id="productCategory" v-model=" addFormData.category ">
                              <option value="" selected disabled hidden>
                                Choose here
                              </option>
                              <option value="bags">Bags</option>
                              <option value="fans">Fans</option>
                              <option value="hats">Hats</option>
                              <option value="hoodies">Hoodies</option>
                              <option value="lanyards">Lanyards</option>
                              <option value="masks">Masks</option>
                              <option value="shirt">Shirt</option>
                              <option value="sweaters">Sweaters</option>
                            </select>
                            <div v-for=" error  in  addForm$.category.$errors " :key=" error.$uid " class="alert alert-danger">
                              <em> {{ error.$message }}</em>
                            </div>
                          </div>

                          <!-- Product name -->
                          <div class="mb-3">
                            <label for="productName" class="form-label">Name</label>
                            <input type="text" class="form-control" id="productName" v-model=" addFormData.name "
                              @input=" addForm$.name.$touch " />
                            <div v-for=" error  in  addForm$.name.$errors " :key=" error.$uid " class="alert alert-danger">
                              <em> {{ error.$message }}</em>
                            </div>
                          </div>

                          <!-- Add price validation (i.e., not greater than + negative) -->
                          <!-- Product nonmember price -->
                          <label for="productPriceNonMember" class="form-label">Price (Non-member)</label>
                          <div class="mb-3">
                            <div class="input-group">
                              <span class="input-group-text">₱</span>
                              <input type="number" class="form-control" id="productPriceNonMember"
                                v-model=" addFormData.priceNonMember " @input=" addForm$.priceNonMember.$touch " />
                            </div>
                            <div v-for=" error  in  addForm$.priceNonMember.$errors " :key=" error.$uid "
                              class="alert alert-danger">
                              <em> {{ error.$message }}</em>
                            </div>
                          </div>

                          <!-- Product member price -->
                          <label for="productPriceMember" class="form-label">Price (Member)</label>
                          <div class="mb-3">
                            <div class="input-group">
                              <span class="input-group-text">₱</span>
                              <input type="number" class="form-control" id="productPriceMember"
                                v-model=" addFormData.priceMember " @input=" addForm$.priceMember.$touch " />
                            </div>
                            <div v-for=" error  in  addForm$.priceMember.$errors " :key=" error.$uid "
                              class="alert alert-danger">
                              <em> {{ error.$message }}</em>
                            </div>
                          </div>

                          <!-- Product stocks -->
                          <div class="mb-3">
                            <label for="productStocks" class="form-label">Stocks</label>
                            <input type="number" class="form-control" id="productStocks" v-model=" addFormData.Stocks "
                              @input=" addForm$.Stocks.$touch " />
                            <div v-for=" error  in  addForm$.Stocks.$errors " :key=" error.$uid " class="alert alert-danger">
                              <em> {{ error.$message }}</em>
                            </div>
                          </div>

                          <!-- Product Description -->
                          <div class="mb-3">
                            <label for="productDescription" class="form-label">Description</label>
                            <textarea class="form-control mb-1" id="productDescription" v-model=" addFormData.description "
                              placeholder="Enter a description" @input=" addForm$.description.$touch " rows="7"></textarea>

                            <div v-for=" error  in  addForm$.description.$errors " :key=" error.$uid "
                              class="alert alert-danger">
                              <em> {{ error.$message }}</em>
                            </div>
                          </div>
                        </form>
                      </v-card-text>
                      <v-card-actions class="justify-end">
                        <v-btn variant="text" @click=" submitAddForm ">Save</v-btn>
                      </v-card-actions>
                    </v-card>
                  </template>
                </v-dialog>
              </div>
              <div class="table-container">
                <EasyDataTable style="width: 100%" :headers=" headers " :items=" products " alternating buttons-pagination
                  :search-field=" searchField " :search-value=" searchValue " :rows-per-page=" 5 " theme-color="#5a2f3c">
                  <template #item-operation=" item ">
                    <span v-bind=" props " class="fas fa-edit ms-2" @click=" editRow(item) " type="button"></span>
                    <span class="fas fa-trash ms-2" @click=" deleteRow(item) " type="button"></span>
                  </template>
                  <template #item-priceMember=" item ">
                    ₱{{ item.priceMember }}
                  </template>
                  <template #item-priceNonMember=" item ">
                    ₱{{ item.priceNonMember }}
                  </template>
                </EasyDataTable>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
    <template v-slot:default=" { isActive } ">
      <v-card>
        <v-toolbar :color=" '#2F437A' ">
          <v-toolbar-title class="text-white">Edit Product</v-toolbar-title>
          <v-toolbar-items>
            <v-btn @click="
              isActive.value = false;
              resetEditForm();
            ">
              <span class="fa-fw text-white fa-solid fa-xmark"></span>
            </v-btn>
          </v-toolbar-items>
        </v-toolbar>
        <v-card-text>
          <!-- Set novalidate because of custom validations -->
          <form novalidate>
            <!-- No image uploaded -->
            <div class="mb-3" v-if=" !uploadedProductPhotoUrl ">
              <label class="form-label">Product Photo</label>
              <label for="productPhoto" class="form-control btn btn-primary">Upload</label>
              <input type="file" @change="
                onFileChangeEdit($event);
                previewPhotoUrl($event);
              " class="form-control d-none" id="productPhoto" accept="image/*" />
              <div v-for=" error  in  editForm$.imageRef.$errors " :key=" error.$uid " class="alert alert-danger">
                <em> {{ error.$message }} </em>
              </div>
            </div>

            <!-- Has image -->
            <div class="mb-3" v-else>
              <label class="form-label">Product Photo</label>
              <!-- TODO: Don't use br -->
              <br />
              <a class="form-text" :href=" uploadedProductPhotoUrl " target="_blank">
                {{ uploadedProductPhoto.name }}
              </a>
              <!-- Delete photo -->
              <span type="button" class="fa-solid fa-xmark fa-fw" @click="
                deleteProductPhotoUrl();
                deleteFileEdit();
              "></span>
            </div>

            <!-- Product Category -->
            <div class="mb-3">
              <label for="productCategory" class="form-label">Category</label>
              <select class="form-control" id="productCategory" v-model=" editFormData.category ">
                <option value="" selected disabled hidden>Choose here</option>
                <option value="bags">Bags</option>
                <option value="fans">Fans</option>
                <option value="hats">Hats</option>
                <option value="hoodies">Hoodies</option>
                <option value="lanyards">Lanyards</option>
                <option value="masks">Masks</option>
                <option value="shirt">Shirt</option>
                <option value="sweaters">Sweaters</option>
              </select>
              <div v-for=" error  in  editForm$.category.$errors " :key=" error.$uid " class="alert alert-danger">
                <em> {{ error.$message }}</em>
              </div>
            </div>

            <!-- Product name -->
            <div class="mb-3">
              <label for="productName" class="form-label">Name</label>
              <input type="text" class="form-control" id="productName" v-model=" editFormData.name "
                @input=" editForm$.name.$touch " />
              <div v-for=" error  in  editForm$.name.$errors " :key=" error.$uid " class="alert alert-danger">
                <em> {{ error.$message }}</em>
              </div>
            </div>

            <!-- Add price validation (i.e., not greater than + negative) -->
            <!-- Product nonmember price -->
            <label for="productPriceNonMember" class="form-label">Price (Non-member)</label>
            <div class="mb-3">
              <div class="input-group">
                <span class="input-group-text">₱</span>
                <input type="number" class="form-control" id="productPriceNonMember" v-model=" editFormData.priceNonMember "
                  @input=" editForm$.priceNonMember.$touch " />
              </div>
              <div v-for=" error  in  editForm$.priceNonMember.$errors " :key=" error.$uid " class="alert alert-danger">
                <em> {{ error.$message }}</em>
              </div>
            </div>

            <!-- Product member price -->
            <label for="productPriceMember" class="form-label">Price (Member)</label>
            <div class="mb-3">
              <div class="input-group">
                <span class="input-group-text">₱</span>
                <input type="number" class="form-control" id="productPriceMember" v-model=" editFormData.priceMember "
                  @input=" editForm$.priceMember.$touch " />
              </div>
              <div v-for=" error  in  editForm$.priceMember.$errors " :key=" error.$uid " class="alert alert-danger">
                <em> {{ error.$message }}</em>
              </div>
            </div>

            <!-- Product stocks -->
            <div class="mb-3">
              <label for="productStocks" class="form-label">Stocks</label>
              <input type="number" class="form-control" id="productStocks" v-model=" editFormData.Stocks "
                @input=" editForm$.Stocks.$touch " />
              <div v-for=" error  in  editForm$.Stocks.$errors " :key=" error.$uid " class="alert alert-danger">
                <em> {{ error.$message }}</em>
              </div>
            </div>

            <!-- Product Description -->
            <div class="mb-3">
              <label for="productDescription" class="form-label">Description</label>
              <textarea class="form-control mb-1" id="productDescription" v-model=" editFormData.description "
                placeholder="Enter a description" @input=" editForm$.description.$touch " rows="7"></textarea>

              <div v-for=" error  in  editForm$.description.$errors " :key=" error.$uid " class="alert alert-danger">
                <em> {{ error.$message }}</em>
              </div>
            </div>
          </form>
        </v-card-text>
        <v-card-actions class="justify-end">
          <v-btn variant="text" @click=" submitEditForm(item.ID) ">Save</v-btn>
        </v-card-actions>
      </v-card>
    </template>
  </v-dialog>
</template>

<script>
import { ref, onMounted, onUnmounted, watch, reactive } from "vue";
import { db, uploadToStorage } from "@/firebase";
import {
  collection,
  onSnapshot,
  query,
  updateDoc,
  doc,
  deleteDoc,
  addDoc,
} from "@firebase/firestore";
import {
  getStorage,
  ref as storageRef,
  uploadBytesResumable,
  deleteObject,
} from "@firebase/storage";
import useVuelidate from "@vuelidate/core";
import { required, helpers, minLength, maxLength } from "@vuelidate/validators";
import { toast } from "vue3-toastify";

export default {
  data() {
    return {
      uploadedProductPhoto: null,
      uploadedProductPhotoUrl: null,
      item: null,
    };
  },
  setup() {
    const storage = getStorage();
    const loading = ref(true);
    const loadingForm = ref(false);

    // Table headers
    const headers = ref([
      { text: "Name", value: "name" },
      { text: "Price (Non-member)", value: "priceNonMember" },
      { text: "Price (Member)", value: "priceMember" },
      { text: "Stocks", value: "Stocks" },
      { text: "Operation", value: "operation" },
    ]);

    // Table data
    const products = ref([]);
    let unsubscribe;

    // Firestore snapshot
    onMounted(() => {
      const q = query(collection(db, "Products"));
      unsubscribe = onSnapshot(q, (snapshot) => {
        loading.value = true;
        products.value = snapshot.docs.map((doc) => ({
          id: doc.id,
          ...doc.data(),
        }));
        loading.value = false;
      });
    });

    // Unsubscribe from snapshot
    onUnmounted(() => {
      unsubscribe();
    });

    // Search handling
    const searchField = ["name"];
    const searchValue = ref("");

    ////// FORMS

    // Dialog handling
    const editDialog = ref(false);
    const addDialog = ref(false);

    // Edit form data
    const editFormData = reactive({
      imageRef: "",
      name: "",
      category: "",
      priceMember: "",
      priceNonMember: "",
      Stocks: "",
      description: "",
    });

    // Add form data
    const addFormData = reactive({
      imageRef: "",
      name: "",
      category: "",
      priceMember: "",
      priceNonMember: "",
      Stocks: "",
      description: "",
    });

    // Watch for updates
    watch(
      () => products,
      (newVal) => {
        editFormData.imageRef = newVal.imageRef;
        editFormData.name = newVal.name;
        editFormData.category = newVal.category;
        editFormData.priceMember = newVal.priceMember;
        editFormData.priceNonMember = newVal.priceNonMember;
        editFormData.Stocks = newVal.Stocks;
        editFormData.description = newVal.description;
      },
      { deep: true }
    );

    // Custom rule for numbers
    const nonNegative = (value) => {
      return value >= 0;
    };

    const isLesserThanPriceNonMemberEdit = (value) => {
      return value < editFormData.priceNonMember;
    };

    const isLesserThanPriceNonMemberAdd = (value) => {
      return value < addFormData.priceNonMember;
    };

    // Vuelidate rules
    const editFormRules = {
      imageRef: {
        required: helpers.withMessage("An image is required", required),
      },
      name: { required, minLength: minLength(3), maxLength: maxLength(30) },
      category: { required },
      priceMember: {
        required,
        minLength: minLength(1),
        maxLength: maxLength(5),
        nonNegative: helpers.withMessage(
          "Price cannot be negative",
          nonNegative
        ),
        isLesserThanPriceNonMemberEdit: helpers.withMessage(
          "Price cannot be greater than price for non-members",
          isLesserThanPriceNonMemberEdit
        ),
      },
      priceNonMember: {
        required,
        minLength: minLength(1),
        maxLength: maxLength(5),
        nonNegative: helpers.withMessage(
          "Price cannot be negative",
          nonNegative
        ),
      },
      Stocks: {
        required,
        minLength: minLength(1),
        maxLength: maxLength(5),
        nonNegative: helpers.withMessage(
          "Stocks cannot be negative",
          nonNegative
        ),
      },
      description: {
        required,
        minLength: minLength(3),
        maxLength: maxLength(1000),
      },
    };
    const addFormRules = {
      imageRef: {
        required: helpers.withMessage("An image is required", required),
      },
      name: { required, minLength: minLength(3), maxLength: maxLength(30) },
      category: { required },
      priceMember: {
        required,
        minLength: minLength(1),
        maxLength: maxLength(5),
        nonNegative: helpers.withMessage(
          "Price cannot be negative",
          nonNegative
        ),
        isLesserThanPriceNonMemberAdd: helpers.withMessage(
          "Price cannot be greater than price for non-members",
          isLesserThanPriceNonMemberAdd
        ),
      },
      priceNonMember: {
        required,
        minLength: minLength(1),
        maxLength: maxLength(5),
        nonNegative: helpers.withMessage(
          "Price cannot be negative",
          nonNegative
        ),
      },
      Stocks: {
        required,
        minLength: minLength(1),
        maxLength: maxLength(5),
        nonNegative: helpers.withMessage(
          "Stocks cannot be negative",
          nonNegative
        ),
      },
      description: {
        required,
        minLength: minLength(3),
        maxLength: maxLength(1000),
      },
    };

    // Vuelidate init
    const editForm$ = useVuelidate(editFormRules, editFormData);
    const addForm$ = useVuelidate(addFormRules, addFormData);

    // If x mark is clicked, discard changes
    const resetEditForm = () => {
      editFormData.imageRef = "";
      editFormData.name = "";
      editFormData.category = "";
      editFormData.priceMember = "";
      editFormData.priceNonMember = "";
      editFormData.Stocks = "";

      // Reset errors
      editForm$.value.$reset();
    };
    const resetAddForm = () => {
      addFormData.imageRef = "";
      addFormData.name = "";
      addFormData.category = "";
      addFormData.priceMember = "";
      addFormData.priceNonMember = "";
      addFormData.Stocks = "";

      // Reset errors
      addForm$.value.$reset();
    };

    // File validation and event listeners
    const onFileChangeAdd = (event) => {
      addFormData.imageRef = event.target.files[0];
    };

    const deleteFileAdd = () => {
      addFormData.imageRef = null;
    };

    const onFileChangeEdit = (event) => {
      editFormData.imageRef = event.target.files[0];
    };

    const deleteFileEdit = () => {
      editFormData.imageRef = null;
    };

    const submitEditForm = async (productId) => {
      loadingForm.value = true;
      const result = await editForm$.value.$validate();

      if (result) {
        editForm$.value.$reset();

        let productPhotoDownloadUrl = null;

        // If new image is uploaded, it is of instance file.
        if (!(editFormData.imageRef instanceof File)) {
          productPhotoDownloadUrl = editFormData.imageRef;
        } else {
          const storageProductPhotoRef = storageRef(
            storage,
            "products/" + productId + ".png"
          );
          const productPhotoUploadTask = uploadBytesResumable(
            storageProductPhotoRef,
            editFormData.imageRef
          );
          productPhotoDownloadUrl = await uploadToStorage(
            productPhotoUploadTask
          );
        }

        await updateDoc(doc(db, "Products", productId), {
          name: editFormData.name,
          alias: editFormData.name.toLowerCase(),
          category: editFormData.category,
          priceMember: editFormData.priceMember,
          priceNonMember: editFormData.priceNonMember,
          Stocks: editFormData.Stocks,
          imageRef: productPhotoDownloadUrl,
          description: editFormData.description,
        });

        toast.success("Product updated successfully!", {
          autoClose: 2000,
        });

        editDialog.value = false;
      } else {
        toast.error("Unable to save details");
      }

      loadingForm.value = false;
    };

    const submitAddForm = async () => {
      loadingForm.value = true;
      const result = await addForm$.value.$validate();
      console.log(addFormData.imageRef);
      console.log(addForm$);

      console.log(result);

      if (result) {
        addForm$.value.$reset();

        const colRef = collection(db, "Products");
        const docRef = await addDoc(colRef, {
          name: addFormData.name,
          alias: addFormData.name.toLowerCase(),
          category: addFormData.category,
          priceMember: addFormData.priceMember,
          priceNonMember: addFormData.priceNonMember,
          Stocks: addFormData.Stocks,
          description: addFormData.description,
        });

        console.log();

        const storageProductPhotoRef = storageRef(
          storage,
          "products/" + docRef.id + ".png"
        );
        const productPhotoUploadTask = uploadBytesResumable(
          storageProductPhotoRef,
          addFormData.imageRef
        );
        const productPhotoDownloadUrl = await uploadToStorage(
          productPhotoUploadTask
        );

        await updateDoc(docRef, {
          ID: docRef.id,
          imageRef: productPhotoDownloadUrl,
        });

        addDialog.value = false;

        toast.success("Product successfully added", {
          autoClose: 2000,
        });
      } else {
        toast.error("Unable to add product");
      }

      loadingForm.value = false;
    };

    return {
      products,
      searchField,
      searchValue,
      headers,
      editForm$,
      addForm$,
      editDialog,
      addDialog,
      editFormData,
      addFormData,
      resetEditForm,
      resetAddForm,
      onFileChangeAdd,
      deleteFileAdd,
      onFileChangeEdit,
      deleteFileEdit,
      submitEditForm,
      submitAddForm,
      loading,
      loadingForm
    };
  },
  methods: {
    async editRow(item) {
      console.log(item);
      this.item = item;
      this.editDialog = true;

      const getProductPhoto = async () => {
        const storage = getStorage();
        return storageRef(storage, item.imageRef);
      };

      // Set modal data:
      this.editFormData.name = item.name;
      this.editFormData.category = item.category;
      this.editFormData.priceMember = item.priceMember;
      this.editFormData.priceNonMember = item.priceNonMember;
      this.editFormData.Stocks = item.Stocks;
      this.editFormData.imageRef = item.imageRef;
      this.editFormData.description = item.description;

      // Set image handler data:
      this.uploadedProductPhoto = await getProductPhoto();
      this.uploadedProductPhotoUrl = item.imageRef;
    },
    addRow() {
      this.addDialog = true;

      // Set image handler data:
      this.uploadedProductPhoto = null;
      this.uploadedProductPhotoUrl = null;
    },
    previewPhotoUrl(event) {
      this.uploadedProductPhoto = event.target.files[0];
      this.uploadedProductPhotoUrl = URL.createObjectURL(event.target.files[0]);
    },
    deleteProductPhotoUrl() {
      this.uploadedProductPhoto = null;
      this.uploadedProductPhotoUrl = null;
    },
    async deleteRow(item) {
      console.log(item);

      const storage = getStorage();

      await deleteDoc(doc(db, "Products", item.id));

      await deleteObject(storageRef(storage, "products/" + item.id + ".png"))
        .then(() => {
          toast.success("Product successfully deleted", {
            autoClose: 2000,
          });
        })
        .catch((error) => {
          console.log(error);
          toast.error("Unable to delete product photo");
        });
    },
  },
};
</script>

<style>
.table-container {
  width: 980px;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.wrapper {
  width: 1080px;
  margin: 0 auto;
}

.search {
  width: 30%;
}
</style>
