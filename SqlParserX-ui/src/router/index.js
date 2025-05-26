import Vue from 'vue'
import Router from 'vue-router'
import Home from '@/components/Home.vue'
import LineageVisualizer from '@/components/LineageVisualizer.vue'
import SQLSplitterTester from '@/components/SQLSplitterTester.vue'
import SyntaxValidator from '@/components/SyntaxValidator.vue'  // Add this import

Vue.use(Router)

export default new Router({
    // mode: 'history',
    routes: [
        {
            path: '/',
            name: 'Home',
            component: Home
        },
        {
            path: '/lineage',
            name: 'LineageVisualizer',
            component: LineageVisualizer
        },
        {
            path: '/sql-splitter',
            name: 'SQLSplitterTester',
            component: SQLSplitterTester
        },
        // Add this new route
        {
            path: '/syntax-validator',
            name: 'SyntaxValidator',
            component: SyntaxValidator
        }
    ]
})