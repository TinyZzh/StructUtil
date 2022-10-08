package org.struct.examples.csv;

import org.struct.annotation.StructSheet;
import org.struct.spring.annotation.AutoStruct;

/**
 * @author TinyZ
 * @date 2022-10-08
 */
@AutoStruct()
@StructSheet(fileName = "test_csv_utf16be_bom.csv")
public record CsvUtf16beBomInfo(int id, String name) {

}
