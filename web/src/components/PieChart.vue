<template>
  <div>
    <canvas ref="chart"></canvas>
  </div>
</template>

<script>
import {
  Chart,
  PieController,
  ArcElement,
  Legend,
  Tooltip,
  Title,
} from "chart.js";
Chart.register(PieController, ArcElement, Legend, Tooltip, Title);

export default {
  name: "PieChart",
  props: {
    title: {
      type: String,
      default: "My Pie Chart",
    },
    labels: {
      type: Array,
      default: () => ["Red", "Blue", "Yellow"],
    },
    data: {
      type: Array,
      default: () => [300, 50, 100],
    },
    datasetLabel: {
      type: String,
      default: "My First Dataset",
    },
  },
  data() {
    return {
      chart: null,
    };
  },
  mounted() {
    const data = {
      labels: this.labels,
      datasets: [
        {
          label: this.datasetLabel,
          data: this.data,
          backgroundColor: [
            "rgb(72, 83, 108)",
            "rgb(106, 103, 135)",
            "rgb(145, 124, 160)",
            "rgb(186, 144, 180)",
          ],
        },
      ],
    };

    const config = {
      type: "pie",
      data,
      options: {
        responsive: true,
        width: 300,
        height: 300,
        plugins: {
          title: {
            display: true,
            text: this.title,
          },
        },
      },
    };

    this.chart = new Chart(this.$refs.chart.getContext("2d"), config);
  },
};
</script>
