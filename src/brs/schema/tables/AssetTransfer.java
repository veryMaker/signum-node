/*
 * This file is generated by jOOQ.
 */
package brs.schema.tables;


import brs.schema.Db;
import brs.schema.Indexes;
import brs.schema.Keys;
import brs.schema.tables.records.AssetTransferRecord;

import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row8;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AssetTransfer extends TableImpl<AssetTransferRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>DB.asset_transfer</code>
     */
    public static final AssetTransfer ASSET_TRANSFER = new AssetTransfer();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AssetTransferRecord> getRecordType() {
        return AssetTransferRecord.class;
    }

    /**
     * The column <code>DB.asset_transfer.db_id</code>.
     */
    public final TableField<AssetTransferRecord, Long> DB_ID = createField(DSL.name("db_id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>DB.asset_transfer.id</code>.
     */
    public final TableField<AssetTransferRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>DB.asset_transfer.asset_id</code>.
     */
    public final TableField<AssetTransferRecord, Long> ASSET_ID = createField(DSL.name("asset_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>DB.asset_transfer.sender_id</code>.
     */
    public final TableField<AssetTransferRecord, Long> SENDER_ID = createField(DSL.name("sender_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>DB.asset_transfer.recipient_id</code>.
     */
    public final TableField<AssetTransferRecord, Long> RECIPIENT_ID = createField(DSL.name("recipient_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>DB.asset_transfer.quantity</code>.
     */
    public final TableField<AssetTransferRecord, Long> QUANTITY = createField(DSL.name("quantity"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>DB.asset_transfer.timestamp</code>.
     */
    public final TableField<AssetTransferRecord, Integer> TIMESTAMP = createField(DSL.name("timestamp"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>DB.asset_transfer.height</code>.
     */
    public final TableField<AssetTransferRecord, Integer> HEIGHT = createField(DSL.name("height"), SQLDataType.INTEGER.nullable(false), this, "");

    private AssetTransfer(Name alias, Table<AssetTransferRecord> aliased) {
        this(alias, aliased, null);
    }

    private AssetTransfer(Name alias, Table<AssetTransferRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>DB.asset_transfer</code> table reference
     */
    public AssetTransfer(String alias) {
        this(DSL.name(alias), ASSET_TRANSFER);
    }

    /**
     * Create an aliased <code>DB.asset_transfer</code> table reference
     */
    public AssetTransfer(Name alias) {
        this(alias, ASSET_TRANSFER);
    }

    /**
     * Create a <code>DB.asset_transfer</code> table reference
     */
    public AssetTransfer() {
        this(DSL.name("asset_transfer"), null);
    }

    public <O extends Record> AssetTransfer(Table<O> child, ForeignKey<O, AssetTransferRecord> key) {
        super(child, key, ASSET_TRANSFER);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Db.DB;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.ASSET_TRANSFER_ASSET_TRANSFER_ASSET_ID_IDX, Indexes.ASSET_TRANSFER_ASSET_TRANSFER_HEIGHT_IDX, Indexes.ASSET_TRANSFER_ASSET_TRANSFER_ID_IDX, Indexes.ASSET_TRANSFER_ASSET_TRANSFER_RECIPIENT_ID_IDX, Indexes.ASSET_TRANSFER_ASSET_TRANSFER_SENDER_ID_IDX);
    }

    @Override
    public Identity<AssetTransferRecord, Long> getIdentity() {
        return (Identity<AssetTransferRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<AssetTransferRecord> getPrimaryKey() {
        return Keys.KEY_ASSET_TRANSFER_PRIMARY;
    }

    @Override
    public AssetTransfer as(String alias) {
        return new AssetTransfer(DSL.name(alias), this);
    }

    @Override
    public AssetTransfer as(Name alias) {
        return new AssetTransfer(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public AssetTransfer rename(String name) {
        return new AssetTransfer(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public AssetTransfer rename(Name name) {
        return new AssetTransfer(name, null);
    }

    // -------------------------------------------------------------------------
    // Row8 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row8<Long, Long, Long, Long, Long, Long, Integer, Integer> fieldsRow() {
        return (Row8) super.fieldsRow();
    }
}
