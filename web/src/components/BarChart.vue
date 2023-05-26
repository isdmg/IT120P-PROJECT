<template>
  <div>
    <canvas ref="chart"></canvas>
  </div>
</template>

<script>
import {
  Chart,
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  Legend,
  Tooltip,
  Title,
} from "chart.js";
Chart.register(
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  Legend,
  Tooltip,
  Title
);

export default {
  name: "BarChart",
  props: {
    title: {
      type: String,
      default: "My Bar Chart",
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
          ],
        },
      ],
    };

    const config = {
      type: "bar",
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
          legend: {
            display: false,
          },
        },
        scales: {
          y: {
            beginAtZero: true,
          },
        },
      },
    };

    this.chart = new Chart(this.$refs.chart.getContext("2d"), config);
  },
};
</script>
