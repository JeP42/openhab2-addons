/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smlmeter.internal.connector;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.openmuc.jsml.structures.ASNObject;
import org.openmuc.jsml.structures.Integer32;
import org.openmuc.jsml.structures.Integer64;
import org.openmuc.jsml.structures.Integer8;
import org.openmuc.jsml.structures.SML_File;
import org.openmuc.jsml.structures.SML_GetListRes;
import org.openmuc.jsml.structures.SML_List;
import org.openmuc.jsml.structures.SML_ListEntry;
import org.openmuc.jsml.structures.SML_Message;
import org.openmuc.jsml.structures.SML_Value;
import org.openmuc.jsml.tl.SMLMessageExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SML parser
 *
 * @author pfaffmann - Initial contribution
 *
 */

public class SmlReader {

    private static final int UNIT_Wh = 30;

    private static final int SML_MSG_TYPE_GET_LIST_RESPONSE = 1793;

    private static final byte[] SML_OBJECT_NAME_VENDOR_ID = DatatypeConverter.parseHexBinary("8181C78203FF");

    private static final byte[] SML_OBJECT_NAME_DEVICE_ID = DatatypeConverter.parseHexBinary("0100000009FF");

    private static final byte[] SML_OBJECT_NAME_OBIS_1_8_0 = DatatypeConverter.parseHexBinary("0100010800ff");
    private static final byte[] SML_OBJECT_NAME_OBIS_1_8_1 = DatatypeConverter.parseHexBinary("0100010801ff");

    private static final byte[] SML_OBJECT_NAME_OBIS_2_8_0 = DatatypeConverter.parseHexBinary("0100020800ff");
    private static final byte[] SML_OBJECT_NAME_OBIS_2_8_1 = DatatypeConverter.parseHexBinary("0100020801ff");

    private static final byte[] SML_OBJECT_NAME_OBIS_15_7_0 = DatatypeConverter.parseHexBinary("01000f0700ff");

    protected Logger logger = LoggerFactory.getLogger(SmlReader.class);

    public SmlReader() {
        super();
    }

    /**
     * Parses the given SML File and extracts information from it. Results are returned as MeterSnapshot
     *
     * @param rawSmlFile SML file
     * @return values extracted from the SML file
     * @throws IOException
     * @throws ConnectorException
     */
    public MeterSnapshot read(String rawSmlFile) throws IOException {

        MeterSnapshot snapshot = null;

        SML_File smlFile = this.parseSmlFile(rawSmlFile);
        if (smlFile != null) {
            this.logger.debug("Got SML file...");
            snapshot = new MeterSnapshot();
            List<SML_Message> smlMessages = smlFile.getMessages();
            if (smlMessages != null && smlMessages.size() > 0) {

                for (SML_Message smlMessage : smlMessages) {
                    if (isMessageTypeGetListResponse(smlMessage)) {
                        SML_GetListRes resp = (SML_GetListRes) smlMessage.getMessageBody().getChoice();
                        SML_List smlList = resp.getValList();
                        SML_ListEntry[] valueList = smlList.getValListEntry();
                        for (SML_ListEntry listEntry : valueList) {

                            if (isVendorIdType(listEntry)) {
                                snapshot.setVendorId(listEntry.getValue().getChoice().toString());
                            } else if (isDeviceId(listEntry)) {
                                byte[] myBytes = listEntry.getValue().getChoice().toString()
                                        .getBytes(StandardCharsets.UTF_8);
                                snapshot.setDeviceId(
                                        this.prettyPrintHexString(DatatypeConverter.printHexBinary(myBytes)));
                            } else if (isObis180(listEntry)) {
                                snapshot.setObis180(this.assureKWH(listEntry));
                            } else if (isObis181(listEntry)) {
                                snapshot.setObis181(this.assureKWH(listEntry));
                            } else if (isObis280(listEntry)) {
                                snapshot.setObis280(this.assureKWH(listEntry));
                            } else if (isObis281(listEntry)) {
                                snapshot.setObis281(this.assureKWH(listEntry));
                            } else if (isObis1570(listEntry)) {
                                snapshot.setObis1570(this.getScaledValue(listEntry));
                            } else {
                                logUnsupportedListEntry(listEntry);
                            }
                        }
                    }
                }
            } else {
                this.logger.warn("The given SML file {} does no contain any messages. Ignore and continue...",
                        smlFile.toString());
            }
        }

        return snapshot;
    }

    /**
     * Assures the value in unit "kWH" which depends on the unit send by the meter: if send in "Wh" then div by
     * 1000, if
     * send in "kWh" return unchanged.
     *
     * @param listEntry
     * @return
     */
    private BigDecimal assureKWH(SML_ListEntry listEntry) {

        BigDecimal value = this.getScaledValue(listEntry);

        if (listEntry.getUnit().getVal() == UNIT_Wh) {
            value = value.divide(BigDecimal.valueOf(1000));
        } else {
            // we assume everything except 30 to be "kWh"
            // so nothing to do in this case...
        }

        return value;
    }

    /**
     * Adds '-' between bytes
     *
     * @param printHexBinary
     * @return
     */
    private String prettyPrintHexString(String hexString) {
        return String.join("-", hexString.split("(?<=\\G..)"));
    }

    private BigDecimal getScaledValue(SML_ListEntry listEntry) {
        return this.getNumericValue(listEntry.getValue())
                .scaleByPowerOfTen(new Integer(listEntry.getScaler().getVal()));
    }

    private SML_File parseSmlFile(String rawSmlFile) throws IOException {
        if (rawSmlFile == null) {
            throw new IllegalArgumentException("Argument rawSmlFile must not be null");
        }

        SML_File smlFile = null;

        byte[] smlMessage = this.extractSmlMessageFromFile(rawSmlFile);
        if (smlMessage != null) {
            DataInputStream is = new DataInputStream(new ByteArrayInputStream(smlMessage));

            smlFile = new SML_File();
            while (is.available() > 0) {
                SML_Message message = new SML_Message();
                if (!message.decode(is)) {
                    throw new IOException("Could not decode message");
                }
                smlFile.add(message);
            }
        }

        return smlFile;
    }

    private byte[] extractSmlMessageFromFile(String rawSmlFile) throws IOException {

        try {
            SMLMessageExtractor messageFromFileExtractor = new SMLMessageExtractor(
                    new DataInputStream(new ByteArrayInputStream(DatatypeConverter.parseHexBinary(rawSmlFile))),
                    10000L);
            return messageFromFileExtractor.getSmlMessage();
        } catch (IOException e) {
            this.handleIOException(e);
        } catch (IllegalArgumentException e) {
            this.logger.debug("SML file is not a valid hex string. Ignoring the message... Exception message: {}",
                    e.getMessage());
        }
        return null;
    }

    private void handleIOException(IOException e) throws IOException {
        if (e.getMessage().contains("wrong crc")) {
            this.logger.debug("wrong crc detected for SML message. Ignoring the message... Exception message: {}",
                    e.getMessage());
        } else {
            throw e;
        }
    }

    private BigDecimal getNumericValue(SML_Value smlValue) {
        if (smlValue == null) {
            throw new IllegalArgumentException("Argument smlValue must not be null");
        }
        ASNObject obj = smlValue.getChoice();

        BigDecimal value = null;
        if (obj.getClass().equals(Integer32.class)) {
            Integer32 val = (Integer32) obj;
            value = new BigDecimal(val.getVal());
        } else if (obj.getClass().equals(Integer64.class)) {
            Integer64 val = (Integer64) obj;
            value = new BigDecimal(val.getVal());
        } else if (obj.getClass().equals(Integer8.class)) {
            Integer8 val = (Integer8) obj;
            value = new BigDecimal(val.getVal());
        } else {
            throw new IllegalArgumentException("Argument is not a numeric type (" + obj.getClass().getName() + ")");
        }
        return value;
    }

    private void logUnsupportedListEntry(SML_ListEntry listEntry) {
        logger.debug("SML message contains unsupported list entry type: "
                + listEntry.getObjName().getOctetString().toString());
    }

    private boolean isObis180(SML_ListEntry listEntry) {
        return Arrays.equals(listEntry.getObjName().getOctetString(), SML_OBJECT_NAME_OBIS_1_8_0);
    }

    private boolean isObis181(SML_ListEntry listEntry) {
        return Arrays.equals(listEntry.getObjName().getOctetString(), SML_OBJECT_NAME_OBIS_1_8_1);
    }

    private boolean isObis280(SML_ListEntry listEntry) {
        return Arrays.equals(listEntry.getObjName().getOctetString(), SML_OBJECT_NAME_OBIS_2_8_0);
    }

    private boolean isObis281(SML_ListEntry listEntry) {
        return Arrays.equals(listEntry.getObjName().getOctetString(), SML_OBJECT_NAME_OBIS_2_8_1);
    }

    private boolean isObis1570(SML_ListEntry listEntry) {
        return Arrays.equals(listEntry.getObjName().getOctetString(), SML_OBJECT_NAME_OBIS_15_7_0);
    }

    private boolean isDeviceId(SML_ListEntry listEntry) {
        return Arrays.equals(listEntry.getObjName().getOctetString(), SML_OBJECT_NAME_DEVICE_ID);
    }

    private boolean isVendorIdType(SML_ListEntry listEntry) {
        return Arrays.equals(listEntry.getObjName().getOctetString(), SML_OBJECT_NAME_VENDOR_ID);
    }

    private boolean isMessageTypeGetListResponse(SML_Message smlMessage) {
        return smlMessage.getMessageBody().getTag().getVal() == SML_MSG_TYPE_GET_LIST_RESPONSE;
    }

}
