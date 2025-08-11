import Vue from 'vue'
import Router from 'vue-router'
import Home from '@/components/Home.vue'
import LineageVisualizer from '@/components/LineageVisualizer.vue'
import SQLSplitterTester from '@/components/SQLSplitterTester.vue'
import SyntaxValidator from '@/components/SyntaxValidator.vue'  // Add this import
import TableParser from '@/components/TableParser.vue'  // 添加导入
import MainLayout from '@/layouts/MainLayout.vue';

Vue.use(Router)

export default new Router({
    // mode: 'history',
    routes: [
        {
            path: '/',
            component: MainLayout,  // 使用布局组件
            children: [
                {
                    path: '',
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
                },
                {
                    path: '/table-parser',
                    name: 'TableParser',
                    component: TableParser
                }
            ]
        },
    ]
})