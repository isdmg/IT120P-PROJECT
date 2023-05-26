<template>
  <v-overlay :model-value="isLoadingPaymentMethod &&
    isLoadingOrderStatus &&
    isLoadingMonths &&
    isLoadingOrdersTotal
    " class="align-center justify-center" persistent>
    <v-progress-circular color="brown" indeterminate size="64"></v-progress-circular>
  </v-overlay>
  <v-card class="mx-auto" max-width="700" v-if="!isLoadingPaymentMethod &&
    !isLoadingOrderStatus &&
    !isLoadingMonths &&
    !isLoadingOrdersTotal
    ">
    <v-container fluid class="pa-8">
      <v-row dense>
        <v-col cols="12" class="pb-4">
          <v-card v-if="!isLoadingOrdersTotal" class="mx-auto" max-width="700">
            <v-card-title> Total Sales </v-card-title>

            <v-card-subtitle class="mb-5">
              ₱{{ sumOrdersTotal }}
            </v-card-subtitle>
          </v-card>
        </v-col>
        <v-col cols="6" class="pb-4">
          <v-card class="mx-auto" max-width="350">
            <PieChart v-if="!isLoadingPaymentMethod" :title="paymentMethodTitle" :labels="paymentMethodLabels"
              :data="paymentMethodData.sum" :datasetLabel="paymentMethodDatasetLabel" class="mb-5" />
          </v-card>
        </v-col>
        <v-col cols="6" class="pb-4">
          <v-card class="mx-auto" max-width="350">
            <PieChart v-if="!isLoadingOrderStatus" :title="orderStatusTitle" :labels="orderStatusLabels"
              :data="orderStatusData.sum" :datasetLabel="orderStatusDatasetLabel" class="mb-5" />
          </v-card>
        </v-col>
        <v-col cols="12">
          <v-card class="mx-auto" max-width="700">
            <BarChart v-if="!isLoadingMonths" :title="quarterTotalTitle" :labels="quarterTotalLabels"
              :data="Object.values(sumOrdersQuarterFiltered)" :datasetLabel="quarterTotalDatasetLabel" class="mb-5" />
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </v-card>

  <!-- <h2>
        GCash: {{ sumGcash }}
    </h2>
    <h2>
        Cash on: {{ sumCash }}
    </h2> -->

  <!-- 
    <h2>
        Total: {{ sumOrdersTotal }}
    </h2> -->

  <!-- <h2>
        Pending: {{ sumPending }}
    </h2>
    <h2>
        Ready: {{ sumReady }}
    </h2>
    <h2>
        Delivered: {{ sumDelivered }}
    </h2>
    <h2>
        Cancelled: {{ sumCancelled }}
    </h2> -->
</template>

<script>
import { db } from "@/firebase";
import { collection, onSnapshot, where, query } from "@firebase/firestore";
import { ref, onMounted, onUnmounted, reactive, computed } from "vue";
import PieChart from "@/components/PieChart.vue";
import BarChart from "@/components/BarChart.vue";

export default {
  components: {
    PieChart,
    BarChart,
  },
  setup() {
    const sumOrdersTotal = ref(0);

    const sumGcash = ref(0);
    const sumCash = ref(0);

    const sumPending = ref(0);
    const sumReady = ref(0);
    const sumDelivered = ref(0);
    const sumCancelled = ref(0);

    const sumOrdersQuarterFiltered = reactive({
      Q1: 0,
      Q2: 0,
      Q3: 0,
      Q4: 0,
    });

    let unsubscribeGcashOrdersTotal;
    let unsubscribeCashOrdersTotal;

    let unsubscribeGcashTotal;
    let unsubscribeCashTotal;

    let unsubscribePendingTotal;
    let unsubscribeReadyTotal;
    let unsubscribeDeliveredTotal;
    let unsubscribeCancelledTotal;

    let unsubscribeGcashOrdersTotalMonthFilter;
    let unsubscribeCashOrdersTotalMonthFilter;

    const year = new Date().getFullYear();

    const isLoadingGcashTotal = ref(true);
    const isLoadingCashTotal = ref(true);
    const isLoadingPaymentMethod = computed(
      () => isLoadingGcashTotal.value || isLoadingCashTotal.value
    );

    const isLoadingPendingTotal = ref(true);
    const isLoadingReadyTotal = ref(true);
    const isLoadingDeliveredTotal = ref(true);
    const isLoadingCancelledTotal = ref(true);
    const isLoadingOrderStatus = computed(
      () =>
        isLoadingPendingTotal.value ||
        isLoadingReadyTotal.value ||
        isLoadingDeliveredTotal.value ||
        isLoadingCancelledTotal.value
    );


    const isLoadingGcashOrdersTotal = ref(true);
    const isLoadingCashOrdersTotal = ref(true);
    const isLoadingOrdersTotal = computed(
      () => isLoadingGcashOrdersTotal.value || isLoadingCashOrdersTotal.value
    );

    const isLoadingGcashMonthOrdersTotal = ref(true);
    const isLoadingCashMonthOrdersTotal = ref(true);
    const isLoadingMonths = computed(
      () => isLoadingGcashMonthOrdersTotal.value || isLoadingCashMonthOrdersTotal.value
    );

    // Firestore snapshot
    onMounted(() => {
      const gcashOrdersTotal = query(collection(db, "Orders"),
        where("PaymentMethod", "==", "GCash"),
        where("OrderStatus", "in", ["Pending", "Delivered"])
      );

      const cashOrdersTotal = query(collection(db, "Orders"),
        where("PaymentMethod", "==", "Cash on Pick-up"),
        where("OrderStatus", "==", "Delivered")
      );

      const gCashTotal = query(
        collection(db, "Orders"),
        where("PaymentMethod", "==", "GCash")
      );

      const cashTotal = query(
        collection(db, "Orders"),
        where("PaymentMethod", "==", "Cash on Pick-up")
      );

      const pendingTotal = query(
        collection(db, "Orders"),
        where("OrderStatus", "==", "Pending")
      );

      const readyTotal = query(
        collection(db, "Orders"),
        where("OrderStatus", "==", "Ready for Pick-up")
      );

      const deliveredTotal = query(
        collection(db, "Orders"),
        where("OrderStatus", "==", "Delivered")
      );

      const cancelledTotal = query(
        collection(db, "Orders"),
        where("OrderStatus", "==", "Cancelled")
      );

      const monthGcashOrdersTotalFilter = query(
        collection(db, "Orders"),
        where("PaymentMethod", "==", "GCash"),
        where("OrderStatus", "in", ["Pending", "Delivered"]),
        where("Timestamp", ">=", new Date(`January 1, ${year}`)),
        where("Timestamp", "<", new Date(`January 1, ${year + 1}`))
      );

      const monthCashOrdersTotalFilter = query(
        collection(db, "Orders"),
        where("PaymentMethod", "==", "Cash on Pick-up"),
        where("OrderStatus", "==", "Delivered"),
        where("Timestamp", ">=", new Date(`January 1, ${year}`)),
        where("Timestamp", "<", new Date(`January 1, ${year + 1}`))
      );

      unsubscribeGcashOrdersTotal = onSnapshot(gcashOrdersTotal, (snapshot) => {
        snapshot.docChanges().forEach((change) => {
          if (change.type === "added") {
            sumOrdersTotal.value += change.doc.data().TotalAmount;
          }
          if (change.type === "modified") {
            sumOrdersTotal.value +=
              change.doc.data().TotalAmount - change.oldDoc.data().TotalAmount;
          }
          if (change.type === "removed") {
            sumOrdersTotal.value -= change.doc.data().TotalAmount;
          }
        });
        isLoadingGcashOrdersTotal.value = false;
      });

      unsubscribeCashOrdersTotal = onSnapshot(cashOrdersTotal, (snapshot) => {
        snapshot.docChanges().forEach((change) => {
          if (change.type === "added") {
            sumOrdersTotal.value += change.doc.data().TotalAmount;
          }
          if (change.type === "modified") {
            sumOrdersTotal.value +=
              change.doc.data().TotalAmount - change.oldDoc.data().TotalAmount;
          }
          if (change.type === "removed") {
            sumOrdersTotal.value -= change.doc.data().TotalAmount;
          }
        });
        isLoadingCashOrdersTotal.value = false;
      });

      unsubscribeGcashTotal = onSnapshot(gCashTotal, (snapshot) => {
        sumGcash.value = snapshot.size;
        isLoadingGcashTotal.value = false;
      });

      unsubscribeCashTotal = onSnapshot(cashTotal, (snapshot) => {
        sumCash.value = snapshot.size;
        isLoadingCashTotal.value = false;
      });

      unsubscribePendingTotal = onSnapshot(pendingTotal, (snapshot) => {
        sumPending.value = snapshot.size;
        isLoadingPendingTotal.value = false;
      });

      unsubscribeReadyTotal = onSnapshot(readyTotal, (snapshot) => {
        sumReady.value = snapshot.size;
        isLoadingReadyTotal.value = false;
      });

      unsubscribeDeliveredTotal = onSnapshot(deliveredTotal, (snapshot) => {
        sumDelivered.value = snapshot.size;
        isLoadingDeliveredTotal.value = false;
      });

      unsubscribeCancelledTotal = onSnapshot(cancelledTotal, (snapshot) => {
        sumCancelled.value = snapshot.size;
        isLoadingCancelledTotal.value = false;
      });

      unsubscribeGcashOrdersTotalMonthFilter = onSnapshot(monthGcashOrdersTotalFilter, (snapshot) => {
        snapshot.docChanges().forEach((change) => {
          const month = change.doc.data().Timestamp.toDate().getMonth() + 1;
          let quarter;
          if (month <= 3) {
            quarter = "Q1";
          } else if (month <= 6) {
            quarter = "Q2";
          } else if (month <= 9) {
            quarter = "Q3";
          } else {
            quarter = "Q4";
          }

          if (change.type === "added") {
            sumOrdersQuarterFiltered[quarter] += change.doc.data().TotalAmount;
          }
          if (change.type === "modified") {
            sumOrdersQuarterFiltered[quarter] +=
              change.doc.data().TotalAmount - change.oldDoc.data().TotalAmount;
          }
          if (change.type === "removed") {
            sumOrdersQuarterFiltered[quarter] -= change.doc.data().TotalAmount;
          }
        });
        isLoadingGcashMonthOrdersTotal.value = false;
      });

      unsubscribeCashOrdersTotalMonthFilter = onSnapshot(monthCashOrdersTotalFilter, (snapshot) => {
        snapshot.docChanges().forEach((change) => {
          const month = change.doc.data().Timestamp.toDate().getMonth() + 1;
          let quarter;
          if (month <= 3) {
            quarter = "Q1";
          } else if (month <= 6) {
            quarter = "Q2";
          } else if (month <= 9) {
            quarter = "Q3";
          } else {
            quarter = "Q4";
          }

          if (change.type === "added") {
            sumOrdersQuarterFiltered[quarter] += change.doc.data().TotalAmount;
          }
          if (change.type === "modified") {
            sumOrdersQuarterFiltered[quarter] +=
              change.doc.data().TotalAmount - change.oldDoc.data().TotalAmount;
          }
          if (change.type === "removed") {
            sumOrdersQuarterFiltered[quarter] -= change.doc.data().TotalAmount;
          }
        });
        isLoadingCashMonthOrdersTotal.value = false;
      });
    });

    onUnmounted(() => {
      unsubscribeGcashOrdersTotal();
      unsubscribeCashOrdersTotal();
      unsubscribeGcashTotal();
      unsubscribeCashTotal();
      unsubscribePendingTotal();
      unsubscribeReadyTotal();
      unsubscribeDeliveredTotal();
      unsubscribeCancelledTotal();
      unsubscribeGcashOrdersTotalMonthFilter();
      unsubscribeCashOrdersTotalMonthFilter();
    });

    const paymentMethodTitle = "Payment Methods";
    const paymentMethodLabels = ["GCash", "Cash on Pick-up"];
    const paymentMethodDatasetLabel = "Orders";
    const paymentMethodData = reactive({
      sum: [sumGcash, sumCash],
    });

    const orderStatusTitle = "Order Statuses";
    const orderStatusLabels = [
      "Pending",
      "Ready for Pick-up",
      "Delivered",
      "Cancelled",
    ];
    const orderStatusDatasetLabel = "Orders";
    const orderStatusData = reactive({
      sum: [sumPending, sumReady, sumDelivered, sumCancelled],
    });

    const quarterTotalTitle = `Quarterly Total Sales (${year})`;
    const quarterTotalLabels = ["Q1", "Q2", "Q3", "Q4"];
    const quarterTotalDatasetLabel = "Total Amount (₱)";
    const quarterTotalData = reactive({
      sum: [
        sumOrdersQuarterFiltered.Q1,
        sumOrdersQuarterFiltered.Q2,
        sumOrdersQuarterFiltered.Q3,
        sumOrdersQuarterFiltered.Q4,
      ],
    });

    return {
      sumOrdersQuarterFiltered,
      sumGcash,
      sumCash,
      sumPending,
      sumReady,
      sumDelivered,
      sumCancelled,
      sumOrdersTotal,

      paymentMethodTitle,
      paymentMethodLabels,
      paymentMethodData,
      paymentMethodDatasetLabel,

      orderStatusTitle,
      orderStatusLabels,
      orderStatusData,
      orderStatusDatasetLabel,

      quarterTotalTitle,
      quarterTotalLabels,
      quarterTotalDatasetLabel,
      quarterTotalData,

      isLoadingPaymentMethod,
      isLoadingOrderStatus,
      isLoadingOrdersTotal,
      isLoadingMonths,
    };
  },
};
</script>
