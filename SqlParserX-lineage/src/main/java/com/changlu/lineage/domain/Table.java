package com.changlu.lineage.domain;

import java.util.Objects;

public class Table {

    // 所在catalog
    private String catalog;

    // 表所在数据库
    private String db;

    // 名称
    private String name;

    // 别名
    private String aliasTableName;

    public Table() {
    }

    public Table(String catalog, String db, String name, String aliasTableName) {
        this.catalog = catalog;
        this.db = db;
        this.name = name;
        this.aliasTableName = aliasTableName;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAliasTableName() {
        return aliasTableName;
    }

    public void setAliasTableName(String aliasTableName) {
        this.aliasTableName = aliasTableName;
    }

    public static class TableTransfer{

        /**
         * 将Table对象转换为字符串表示
         * 规则：
         * - 若catalog、db、table都不为空：返回 catalog.db.table
         * - 若只有db、table不为空：返回 db.table
         * - 若只有table不为空：返回 table
         * - 若都为空：返回空字符串
         */
        public static String transfer(Table table) {
            if (table == null) {
                return "";
            }

            String catalog = table.getCatalog();
            String db = table.getDb();
            String name = table.getName();

            boolean hasCatalog = catalog != null && !catalog.isEmpty();
            boolean hasDb = db != null && !db.isEmpty();
            boolean hasName = name != null && !name.isEmpty();

            if (hasCatalog && hasDb && hasName) {
                return catalog + "." + db + "." + name;
            } else if (hasDb && hasName) {
                return db + "." + name;
            } else if (hasName) {
                return name;
            } else {
                return "";
            }
        }

        /**
         * 将字符串解析为Table对象
         * 支持格式：
         * - catalog.db.table
         * - db.table
         * - table
         * @param tableName 表名字符串
         * @return Table对象
         */
        public static Table parse(String tableName) {
            if (tableName == null || tableName.isEmpty()) {
                return new Table(null, null, null, null);
            }

            String[] parts = tableName.split("\\.");
            Table table = new Table();

            if (parts.length == 3) {
                // catalog.db.table 格式
                table.setCatalog(parts[0]);
                table.setDb(parts[1]);
                table.setName(parts[2]);
            } else if (parts.length == 2) {
                // db.table 格式
                table.setDb(parts[0]);
                table.setName(parts[1]);
            } else if (parts.length == 1) {
                // table 格式
                table.setName(parts[0]);
            }

            return table;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        return Objects.equals(catalog, table.catalog) && Objects.equals(db, table.db) && Objects.equals(name, table.name) && Objects.equals(aliasTableName, table.aliasTableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalog, db, name, aliasTableName);
    }

    @Override
    public String toString() {
        return "Table{" +
                "catalog='" + catalog + '\'' +
                ", db='" + db + '\'' +
                ", name='" + name + '\'' +
                ", aliasTableName='" + aliasTableName + '\'' +
                '}';
    }
}
