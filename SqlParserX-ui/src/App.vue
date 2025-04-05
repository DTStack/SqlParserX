<!-- src/App.vue -->
<template>
  <div id="app">
    <LineageVisualizer />
  </div>
</template>

<script>
import LineageVisualizer from './components/LineageVisualizer.vue';

export default {
  name: 'App',
  components: {
    LineageVisualizer
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  height: 100vh;
  width: 100vw;
  margin: 0;
  padding: 0;
}
</style>