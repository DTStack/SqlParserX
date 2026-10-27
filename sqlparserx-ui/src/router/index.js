import Vue from 'vue'
import Router from 'vue-router'
import Home from '@/components/Home.vue'
import LineageVisualizer from '@/components/LineageVisualizer.vue'
import SQLSplitterTester from '@/components/SQLSplitterTester.vue'
import SyntaxValidator from '@/components/SyntaxValidator.vue'  // Add this import
import TableParser from '@/components/TableParser.vue'  // 添加导入
import CreateTableParser from '@/components/CreateTableParser.vue'  // 新增导入
import ParameterWorkbench from '@/components/ParameterWorkbench.vue'
import MainLayout from '@/layouts/MainLayout.vue';

Vue.use(Router)

export default new Router({
    mode: 'history',
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
                    path: '/app/lineage.html',
                    name: 'LineageVisualizer',
                    component: LineageVisualizer
                },
                {
                    path: '/app/sql-splitter.html',
                    name: 'SQLSplitterTester',
                    component: SQLSplitterTester
                },
                // Add this new route
                {
                    path: '/app/syntax-validator.html',
                    name: 'SyntaxValidator',
                    component: SyntaxValidator
                },
                {
                    path: '/app/table-parser.html',
                    name: 'TableParser',
                    component: TableParser
                },                // 新增建表SQL解析路由
                {
                    path: '/app/create-table-parser.html',
                    name: 'CreateTableParser',
                    component: CreateTableParser
                },
                {
                    path: '/app/parameter-workbench.html',
                    name: 'ParameterWorkbench',
                    component: ParameterWorkbench
                }
            ]
        },
    ]
})
