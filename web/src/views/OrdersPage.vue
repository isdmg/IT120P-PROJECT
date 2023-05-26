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
                <input class="float-md-start form-control search" placeholder="Search Order Id / Timestamp" type="text"
                  id="search" style="height: 100%; font-size: small" v-model="searchValue" />
              </div>
              <div class="table-container">
                <EasyDataTable style="width: 100%" :headers="headers" :items="orders" alternating buttons-pagination
                  :search-field="searchField" :search-value="searchValue" :rows-per-page="5" theme-color="#5a2f3c">
                  <template #item-operation="item">
                    <span v-bind="props" class="fas fa-edit ms-5" @click="editRow(item)" type="button"></span>
                  </template>
                  <template #item-TotalAmount="item">
                    ₱{{ item.TotalAmount }}
                  </template>
                </EasyDataTable>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
    <template v-slot:default="{ isActive }">
      <v-card>
        <v-toolbar :color="'#2F437A'">
          <v-toolbar-title class="text-white">Change Order Status</v-toolbar-title>
          <v-toolbar-items>
            <v-btn @click="isActive.value = false;
            resetEditForm();
                                      ">
              <span class="fa-fw text-white fa-solid fa-xmark"></span>
            </v-btn>
          </v-toolbar-items>
        </v-toolbar>
        <v-card-text>
          <!-- Set novalidate because of custom validations -->
          <form novalidate>
            <!-- Order Status -->
            <div class="mb-3">
              <label for="orderStatus" class="form-label">Category</label>
              <select v-if="editFormData.OrderPaymentMethod == 'GCash'" class="form-control" id="orderStatus" v-model=" editFormData.OrderStatus ">
                <option value="" selected disabled hidden>Choose here</option>
                <option value="Pending">Pending</option>
                <option value="Delivered">Delivered</option>
                <option value="Cancelled">Cancelled</option>
              </select>
              <select v-if="editFormData.OrderPaymentMethod == 'Cash on Pick-up'" class="form-control" id="orderStatus" v-model=" editFormData.OrderStatus ">
                <option value="" selected disabled hidden>Choose here</option>
                <option value="Pending">Pending</option>
                <option value="Ready for Pick-up">Ready for Pick-up</option>
                <option value="Delivered">Delivered</option>
                <option value="Cancelled">Cancelled</option>
              </select>
              <div v-for="  error   in   editForm$.OrderStatus.$errors  " :key=" error.$uid " class="alert alert-danger">
                <em> {{ error.$message }}</em>
              </div>
            </div>
          </form>
        </v-card-text>
        <v-card-actions class="justify-end">
          <v-btn variant="text" @click=" submitEditForm(item.OrderId) ">Save</v-btn>
        </v-card-actions>
      </v-card>
    </template>
  </v-dialog>
</template>

<script>
import { ref, onMounted, onUnmounted, watch, reactive } from "vue";
import moment from "moment";
import { db } from "@/firebase";
import {
  collection,
  onSnapshot,
  query,
  updateDoc,
  doc,
  orderBy,
  where,
} from "@firebase/firestore";
import useVuelidate from "@vuelidate/core";
import { required } from "@vuelidate/validators";
import { toast } from "vue3-toastify";

export default {
  data() {
    return {
      item: null,
    };
  },
  setup() {
    const loading = ref(true);
    const loadingForm = ref(false);

    // Table headers
    const headers = ref([
      { text: "Id", value: "OrderId" },
      { text: "Status", value: "OrderStatus" },
      { text: "Payment Method", value: "PaymentMethod" },
      { text: "Timestamp", value: "Timestamp" },
      { text: "Total Amount", value: "TotalAmount" },
      { text: "Account Reference", value: "accountRef" },
      { text: "Operation", value: "operation" },
    ]);

    // Table data
    const orders = ref([]);
    let unsubscribe;

    // Firestore snapshot
    onMounted(() => {
      const q = query(
        collection(db, "Orders"),
        where("OrderStatus", "in", ["Pending", "Ready for Pick-up"]),
        orderBy("Timestamp", "asc")
      );
      unsubscribe = onSnapshot(q, (snapshot) => {
        orders.value = snapshot.docs.map((doc) => {
          let data = doc.data();
          data.Timestamp = moment(
            new Date(data.Timestamp.seconds * 1000)
          ).format("YYYY-MM-DD HH:mm");
          return {
            id: doc.id,
            ...data,
          };
        });
        loading.value = false;
      });
    });

    // Unsubscribe from snapshot
    onUnmounted(() => {
      unsubscribe();
    });

    // Search handling
    const searchField = ["OrderId", "Timestamp"];
    const searchValue = ref("");

    ////// FORMS

    // Dialog handling
    const editDialog = ref(false);

    // Edit form data
    const editFormData = reactive({
      OrderStatus: "",
      OrderPaymentMethod: "",
    });

    // Watch for updates
    watch(
      () => orders,
      (newVal) => {
        editFormData.OrderStatus = newVal.orderStatus;
      },
      { deep: true }
    );

    // Vuelidate rules
    const editFormRules = {
      OrderStatus: { required },
    };

    // Vuelidate init
    const editForm$ = useVuelidate(editFormRules, editFormData);

    // If x mark is clicked, discard changes
    const resetEditForm = () => {
      editFormData.OrderStatus = "";

      // Reset errors
      editForm$.value.$reset();
    };

    const submitEditForm = async (orderId) => {
      loadingForm.value = true;
      const result = await editForm$.value.$validate();

      if (result) {
        // TODO: Do this on TFYO
        editForm$.value.$reset();

        await updateDoc(doc(db, "Orders", orderId), {
          OrderStatus: editFormData.OrderStatus,
        });

        if (
          editFormData.OrderStatus == "Delivered" ||
          editFormData.OrderStatus == "Cancelled"
        ) {
          toast.success("Order updated and archived!", {
            autoClose: 2000,
          });
        } else {
          toast.success("Order updated successfully!", {
            autoClose: 2000,
          });
        }

        editDialog.value = false;
      } else {
        toast.error("Unable to save details");
      }
      loadingForm.value = false;
    };

    return {
      orders,
      searchField,
      searchValue,
      headers,
      editForm$,
      editDialog,
      editFormData,
      resetEditForm,
      submitEditForm,
      loading,
      loadingForm
    };
  },
  methods: {
    async editRow(item) {
      console.log(item);
      this.item = item;
      this.editDialog = true;

      // Set modal data:
      this.editFormData.OrderStatus = item.OrderStatus;
      this.editFormData.OrderPaymentMethod = item.PaymentMethod;
    },
  },
};
</script>

<style>
.table-container {
  width: 1020px;
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
