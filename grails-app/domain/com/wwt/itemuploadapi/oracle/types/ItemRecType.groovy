package com.wwt.itemuploadapi.oracle.types

import java.sql.SQLData
import java.sql.SQLException
import java.sql.SQLInput
import java.sql.SQLOutput

class ItemRecType implements SQLData, Serializable {

    String sql_type = 'APPS.WWT_ITEM_CREATION_RECTYPE'
    String segment2
    String segment3
    String segment1
    String segment4
    String organizationCode
    String transactionType
    String description
    String primaryUomCode
    Integer serialNumberControlCode
    Integer templateId
    String attribute1
    String attribute15

    @Override
    String getSQLTypeName() throws SQLException {
        return sql_type
    }

    @Override
    void readSQL(SQLInput stream, String typeName) throws SQLException {

    }

    @Override
    void writeSQL(SQLOutput stream) throws SQLException {
        stream.with {
            writeString(this.segment2)
            writeString(this.segment3)
            writeString(this.segment1)
            writeString(this.segment4)
            writeString(this.organizationCode)
            writeString(this.transactionType)
            writeString(this.description)
            writeString(this.primaryUomCode)
            writeInt(this.serialNumberControlCode)
            writeInt(this.templateId)
            writeString(this.attribute1)
            writeString(this.attribute15)
        }
    }
}
