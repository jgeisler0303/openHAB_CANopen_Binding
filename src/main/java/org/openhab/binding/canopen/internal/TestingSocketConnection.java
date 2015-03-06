package org.openhab.binding.canopen.internal;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.entropia.can.CanSocket;
import de.entropia.can.CanSocket.CanId;
import de.entropia.can.CanSocket.CanFrame;
import de.entropia.can.CanSocket.CanInterface;
import de.entropia.can.CanSocket.Mode;

public class TestingSocketConnection implements ISocketConnection {
	
	private static final Logger logger = LoggerFactory.getLogger(TestingSocketConnection.class);

	private Set<CANMessageReceivedListener> listeners = new HashSet<>();
	
	private ReaderThread readingThread;
	
	private static final List<TestCANMessage> testMessages= new ArrayList<TestCANMessage>();
	private static final Map<String, SDOResponse> sdoResponse= new HashMap<String, SDOResponse>();
	private TestCANMessage currMessage= null;
	private int testMessagesIndex= -10;
		
	public TestingSocketConnection() {
		super();
		// bool: Number CANopenTest_bool "test of bool [%d]" {canopen="if:vcan0,type:boolean,txpdo:0x181,txofs:1"}
		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 1, new byte[] {(byte)0xFF, (byte)0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF}, "boolean", "0", new byte[] {(byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 1, new byte[] {(byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "boolean", "1", new byte[] {(byte)0x01}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 1, new byte[] {(byte)0x00, (byte)0x7F, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "boolean", "1", new byte[] {(byte)0x01}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 1, new byte[] {(byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "boolean", "1", new byte[] {(byte)0x01}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 1, new byte[] {(byte)0x00, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "boolean", "1", new byte[] {(byte)0x01}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 1, new byte[] {(byte)0x00}, "boolean", "data too short", new byte[] {}));
//		// int8: Number CANopenTest_int8 "test of int8 [%d]" {canopen="if:vcan0,type:int8,txpdo:0x182,txofs:2"}
		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 2, new byte[] {(byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF}, "int8", "0", new byte[] {(byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 2, new byte[] {(byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "int8", "1", new byte[] {(byte)0x01}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 2, new byte[] {(byte)0x00, (byte)0x00, (byte)0x7F, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "int8", "127", new byte[] {(byte)0x7F}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 2, new byte[] {(byte)0x00, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "int8", "-128", new byte[] {(byte)0x80}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 2, new byte[] {(byte)0x00, (byte)0x00, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "int8", "-1", new byte[] {(byte)0xFF}));
//		// uint8: Number CANopenTest_uint8 "test of uint8 [%d]" {canopen="if:vcan0,type:uint8,txpdo:0x183,txofs:2"}
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 3, new byte[] {(byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF}, "uint8", "0", new byte[] {(byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 3, new byte[] {(byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "uint8", "1", new byte[] {(byte)0x01}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 3, new byte[] {(byte)0x00, (byte)0x00, (byte)0x7F, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "uint8", "127", new byte[] {(byte)0x7F}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 3, new byte[] {(byte)0x00, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "uint8", "128", new byte[] {(byte)0x80}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 3, new byte[] {(byte)0x00, (byte)0x00, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "uint8", "255", new byte[] {(byte)0xFF}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 3, new byte[] {(byte)0x00, (byte)0x00}, "uint8", "too short", new byte[] {}));
//		// int16: Number CANopenTest_int16 "test of int16 [%d]" {canopen="if:vcan0,type:int16,txpdo:0x184,txofs:3"}
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 4, new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF}, "int16", "0", new byte[] {(byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 4, new byte[] {(byte)0x00, (byte)0x00, (byte)0xFF, (byte)0x01, (byte)0x00, (byte)0xFF, (byte)0x00, (byte)0x00}, "int16", "1", new byte[] {(byte)0x01, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 4, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x7F, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "int16", "127", new byte[] {(byte)0x7F, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 4, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x80, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0x00}, "int16", "-128", new byte[] {(byte)0x80, (byte)0xFF}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 4, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0x00}, "int16", "-1", new byte[] {(byte)0xFF, (byte)0xFF}));
//		// uint16: Number CANopenTest_uint16 "test of uint16 [%d]" {canopen="if:vcan0,type:uint16,txpdo:0x185,txofs:6"}
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 5, new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00}, "uint16", "0", new byte[] {(byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 5, new byte[] {(byte)0x00, (byte)0x00, (byte)0xFF, (byte)0x03, (byte)0x05, (byte)0xFF, (byte)0x01, (byte)0x00}, "uint16", "1", new byte[] {(byte)0x01, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 5, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x7F, (byte)0x00}, "uint16", "127", new byte[] {(byte)0x7F, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 5, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xFF, (byte)0x00, (byte)0x80, (byte)0xFF}, "uint16", "65408", new byte[] {(byte)0x80, (byte)0xFF}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 5, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0xFE, (byte)0xFE, (byte)0x00, (byte)0xFF, (byte)0xFF}, "uint16", "65535", new byte[] {(byte)0xFF, (byte)0xFF}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 5, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0xFE, (byte)0xFE, (byte)0x00, (byte)0xFF}, "uint16", "too short", null));
//		// int24: Number CANopenTest_int24 "test of int24 [%d]" {canopen="if:vcan0,type:int24,txpdo:0x186,txofs:4"}
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 6, new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xFF}, "int24", "0", new byte[] {(byte)0x00, (byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 6, new byte[] {(byte)0x00, (byte)0x00, (byte)0xFF, (byte)0x03, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0xFF}, "int24", "1", new byte[] {(byte)0x01, (byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 6, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x7E, (byte)0x7F, (byte)0x00, (byte)0x00, (byte)0xFF}, "int24", "127", new byte[] {(byte)0x7F, (byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 6, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x81, (byte)0x80, (byte)0xFF, (byte)0xFF, (byte)0x00}, "int24", "-128", new byte[] {(byte)0x80, (byte)0xFF, (byte)0xFF}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 6, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x00}, "int24", "-1", new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF}));
//		// uint24: Number CANopenTest_uint24 "test of uint24 [%d]" {canopen="if:vcan0,type:uint24,txpdo:0x187,txofs:4"}
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 7, new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xFF}, "uint24", "0", new byte[] {(byte)0x00, (byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 7, new byte[] {(byte)0x00, (byte)0x00, (byte)0xFF, (byte)0x03, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0xFF}, "uint24", "1", new byte[] {(byte)0x01, (byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 7, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x7E, (byte)0x7F, (byte)0x00, (byte)0x00, (byte)0xFF}, "uint24", "127", new byte[] {(byte)0x7F, (byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 7, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x81, (byte)0x80, (byte)0xFF, (byte)0xFF, (byte)0x00}, "uint24", "16777088", new byte[] {(byte)0x80, (byte)0xFF, (byte)0xFF}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 7, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x00}, "uint24", "16777215", new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF}));
//		// int32: Number CANopenTest_int32 "test of int32 [%d]" {canopen="if:vcan0,type:int32,txpdo:0x188,txofs:4"}
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 8, new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "int32", "0", new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 8, new byte[] {(byte)0x00, (byte)0x00, (byte)0xFF, (byte)0x03, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00}, "int32", "1", new byte[] {(byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 8, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x7E, (byte)0x7F, (byte)0x00, (byte)0x00, (byte)0x00}, "int32", "127", new byte[] {(byte)0x7F, (byte)0x00, (byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 8, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x81, (byte)0x80, (byte)0xFF, (byte)0xFF, (byte)0xFF}, "int32", "-128", new byte[] {(byte)0x80, (byte)0xFF, (byte)0xFF, (byte)0xFF}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 8, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF}, "int32", "-1", new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 8, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF}, "int32", "too short", null));
//		// uint32: Number CANopenTest_uint32 "test of uint32 [%d]" {canopen="if:vcan0,type:uint32,txpdo:0x189,txofs:4"}
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 9, new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "uint32", "0", new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 9, new byte[] {(byte)0x00, (byte)0x00, (byte)0xFF, (byte)0x03, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00}, "uint32", "1", new byte[] {(byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 9, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x7E, (byte)0x7F, (byte)0x00, (byte)0x00, (byte)0x00}, "uint32", "127", new byte[] {(byte)0x7F, (byte)0x00, (byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 9, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x81, (byte)0x80, (byte)0xFF, (byte)0xFF, (byte)0xFF}, "uint32", "4294967168", new byte[] {(byte)0x80, (byte)0xFF, (byte)0xFF, (byte)0xFF}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 9, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF}, "uint32", "4294967295", new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF}));
//		// int64: Number CANopenTest_int64 "test of int64 [%d]" {canopen="if:vcan0,type:int64,txpdo:0x18A,txofs:4"}
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 10, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "int64", "0", new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 10, new byte[] {(byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "int64", "1", new byte[] {(byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 10, new byte[] {(byte)0x7E, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "int64", "127", new byte[] {(byte)0x7F, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 10, new byte[] {(byte)0x80, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF}, "int64", "-128", new byte[] {(byte)0x80, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 10, new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF}, "int64", "-1", new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF}));
//		
//		// real32: Number CANopenTest_real32 "test of real32 [%f]" {canopen="if:vcan0,real32,txpdo:0x18B,txofs:0"}
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 11, new byte[] {(byte)0x10, (byte)0x06, (byte)0x9E, (byte)0x3F, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "real32", "1.23456", new byte[] {(byte)0x10, (byte)0x06, (byte)0x9E, (byte)0x3F}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 11, new byte[] {(byte)0x54, (byte)0x8F, (byte)0x04, (byte)0x34, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "real32", "1.23456e-7", new byte[] {(byte)0x54, (byte)0x8F, (byte)0x04, (byte)0x34}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 11, new byte[] {(byte)0x00, (byte)0x61, (byte)0x3C, (byte)0x4B, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "real32", "1.23456e7", new byte[] {(byte)0x00, (byte)0x61, (byte)0x3C, (byte)0x4B}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 11, new byte[] {(byte)0x10, (byte)0x06, (byte)0x9E, (byte)0xBF, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "real32", "-1.23456", new byte[] {(byte)0x10, (byte)0x06, (byte)0x9E, (byte)0xBF}));
//		// real64: Number CANopenTest_real64 "test of real64 [%f]" {canopen="if:vcan0,real64,txpdo:0x18C,txofs:0"}
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 12, new byte[] {(byte)0x38, (byte)0x32, (byte)0x8F, (byte)0xFC, (byte)0xC1, (byte)0xC0, (byte)0xF3, (byte)0x3F}, "real32", "1.23456", new byte[] {(byte)0x38, (byte)0x32, (byte)0x8F, (byte)0xFC, (byte)0xC1, (byte)0xC0, (byte)0xF3, (byte)0x3F}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 12, new byte[] {(byte)0x17, (byte)0x5F, (byte)0xCC, (byte)0x75, (byte)0xEA, (byte)0x91, (byte)0x80, (byte)0x3E}, "real32", "1.23456e-7", new byte[] {(byte)0x17, (byte)0x5F, (byte)0xCC, (byte)0x75, (byte)0xEA, (byte)0x91, (byte)0x80, (byte)0x3E}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 12, new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20, (byte)0x8C, (byte)0x67, (byte)0x41}, "real32", "1.23456e7", new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20, (byte)0x8C, (byte)0x67, (byte)0x41}));
//		testMessages.add(new TestCANMessage(CANOpenItemConfig.TPDO1_ID + 12, new byte[] {(byte)0x38, (byte)0x32, (byte)0x8F, (byte)0xFC, (byte)0xC1, (byte)0xC0, (byte)0xF3, (byte)0xBF}, "real32", "-1.23456", new byte[] {(byte)0x38, (byte)0x32, (byte)0x8F, (byte)0xFC, (byte)0xC1, (byte)0xC0, (byte)0xF3, (byte)0xBF}));

// test SDO
		sdoResponse.put(new String(new byte[] {(byte)0x2F, (byte)0x00, (byte)0x60, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}), new SDOResponse(new byte[] {(byte)0x60, (byte)0x00, (byte)0x60, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "correct"));
		sdoResponse.put(new String(new byte[] {(byte)0x40, (byte)0x00, (byte)0x60, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}), new SDOResponse(new byte[] {(byte)0x4F, (byte)0x00, (byte)0x60, (byte)0x01, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00}, " reading 1"));
		sdoResponse.put(new String(new byte[] {(byte)0x2F, (byte)0x00, (byte)0x60, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}), new SDOResponse(new byte[] {(byte)0x60, (byte)0x00, (byte)0x60, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, "correct"));
// test NMT
		// TODO finish test cases

}

	@Override
	public void open() throws Exception {
		if(readingThread==null) {
			readingThread = new ReaderThread();
			readingThread.start();
			logger.debug("opened test connection");
		}		
	}

	@Override
	public void close() {
		readingThread.doStop();
	}

	@Override
	public void addMessageReceivedListener(CANMessageReceivedListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeMessageReceivedListener(CANMessageReceivedListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void send(CanId canId, byte[] data) {
		if(canId.getCanId_SFF()==0x601) {
			SDOResponse r= sdoResponse.get(new String(data));
			if(r!=null) {
				logger.debug("sending sdo respone: " + r);
				notifyListeners(0x581, false, r.data);
			} else
				logger.debug("no sdo respone found for data" + Util.byteArrayToString(data));
				
		} else {
			if(currMessage!=null) {
	 			if((canId.getCanId_SFF()-0x80)==currMessage.canId) {
					if(equalData(currMessage.expectedResponse, data))
						logger.debug("Received expected copy back of TxPDO");
					else
						logger.debug("Received unexpected copy back of TxPDO: " + Util.byteArrayToString(data));
	 			} else
	 				logger.debug("Received unexpected can id: " + Util.canMessageToSting(canId.getCanId_SFF(), data));
			} else
				logger.debug("Received "+ Util.canMessageToSting(canId.getCanId_SFF(), data));
		}
	}
	
	private boolean equalData(byte[] d1, byte[] d2) {
		boolean e= false;
		if(d1==null && d2== null)
			return true;
		
		if(d1==null || d2==null)
			return false;
		
		if(d1.length!=d2.length)
			return false;
		
		for(int i= 0; i<d1.length; i++)
			if(d1[i]!=d2[i])
				return false;
		
		return true;
	}
	
	private class SDOResponse {
		byte[] data;
		String comment;
		
		public SDOResponse(byte[] data, String comment) {
			super();
			this.data = data;
			this.comment = comment;
		}
		
		public String toString() {
			return comment;
		}
	}
	
	private class ReaderThread extends Thread {
		
		private boolean run = true;
		
		public ReaderThread() {
			super("SocketCan reading thread");
		}
		
		public void run() {
			while (run) {
				if(testMessagesIndex<0)
					testMessagesIndex++;
				else if(testMessagesIndex<testMessages.size()) {
					currMessage= testMessages.get(testMessagesIndex);
					currMessage.dispatch();
					testMessagesIndex++;
				}
				try {
					sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
		
		public void doStop() {
			run = false;
			interrupt();
		}
	}
	
	protected void notifyListeners(int canID, boolean rtr, byte[] data) {
		for (CANMessageReceivedListener listener : listeners) {
			try {
				listener.messageReceived(canID, rtr, data, this);
			} catch (Throwable t) {
				logger.error("Error in the listener for can frames!", t);
			}
		}
	}
	
	private class TestCANMessage {
		private int canId;
		private byte[] data;
		private String dataType;
		private String expectedValue;
		private byte[] expectedResponse;
		
		public TestCANMessage(int canId, byte[] data, String dataType, String expectedValue, byte[] expectedResponse) {
			super();
			this.canId = canId;
			this.data = data;
			this.dataType = dataType;
			this.expectedValue = expectedValue;
			this.expectedResponse = expectedResponse;
		}

		protected void dispatch() {
			logger.info("sending message type " + dataType + " expecting: " + expectedValue);
			notifyListeners(canId, false, data);
		}
	}
}
