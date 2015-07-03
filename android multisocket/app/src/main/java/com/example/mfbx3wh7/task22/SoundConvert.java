package com.example.mfbx3wh7.task22; /**
 *
 * No promises this works. It did work once though.....
 *
 * Copyright Barry Cheetham, Nick Filer, University of Manchester and possibly others!
 *
 */

    /**
     *  Table of conversion values. 256 byte values 0-255 converted by direct lookup
     */
    public class SoundConvert {
        private static final short a_law_to_16_bit_uniform_conversions[] = {
                -5504, -5248, -6016, -5760, -4480, -4224, -4992, -4736, -7552, -7296,
                -8064, -7808, -6528, -6272, -7040, -6784, -2752, -2624, -3008, -2880,
                -2240, -2112, -2496, -2368, -3776, -3648, -4032, -3904, -3264, -3136,
                -3520, -3392, -22016, -20992, -24064, -23040, -17920, -16896, -19968, -18944,
                -30208, -29184, -32256, -31232, -26112, -25088, -28160, -27136, -11008, -10496,
                -12032, -11520, -8960, -8448, -9984, -9472, -15104, -14592, -16128, -15616,
                -13056, -12544, -14080, -13568, -344, -328, -376, -360, -280, -264,
                -312, -296, -472, -456, -504, -488, -408, -392, -440, -424,
                -88, -72, -120, -104, -24, -8, -56, -40, -216, -200,
                -248, -232, -152, -136, -184, -168, -1376, -1312, -1504, -1440,
                -1120, -1056, -1248, -1184, -1888, -1824, -2016, -1952, -1632, -1568,
                -1760, -1696, -688, -656, -752, -720, -560, -528, -624, -592,
                -944, -912, -1008, -976, -816, -784, -880, -848, 5504, 5248,
                6016, 5760, 4480, 4224, 4992, 4736, 7552, 7296, 8064, 7808,
                6528, 6272, 7040, 6784, 2752, 2624, 3008, 2880, 2240, 2112,
                2496, 2368, 3776, 3648, 4032, 3904, 3264, 3136, 3520, 3392,
                22016, 20992, 24064, 23040, 17920, 16896, 19968, 18944, 30208, 29184,
                32256, 31232, 26112, 25088, 28160, 27136, 11008, 10496, 12032, 11520,
                8960, 8448, 9984, 9472, 15104, 14592, 16128, 15616, 13056, 12544,
                14080, 13568, 344, 328, 376, 360, 280, 264, 312, 296,
                472, 456, 504, 488, 408, 392, 440, 424, 88, 72,
                120, 104, 24, 8, 56, 40, 216, 200, 248, 232,
                152, 136, 184, 168, 1376, 1312, 1504, 1440, 1120, 1056,
                1248, 1184, 1888, 1824, 2016, 1952, 1632, 1568, 1760, 1696,
                688, 656, 752, 720, 560, 528, 624, 592, 944, 912,
                1008, 976, 816, 784, 880, 848
        };



        /**
         * Simple method to convert a single byte of A�law data
         * to a 16 bit (short) uniform value.
         * @param in  A�law coded byte value to convert.
         * @return Result of the conversion.
         */
        public short convert_a_law_byte_to_16_bit_uniform(byte in) {
            int index = in & 0x000000FF;

            return a_law_to_16_bit_uniform_conversions[index];
        }


        /**
         * Convert an array of A�law coded 8 bit samples held as a byte array
         * to an output array of 16 bit uniform coded samples but stored in a byte array. Note
         * the conversion followed by mapping the result to two bytes in the result array.
         * @param inArray Array of samples to convert.
         * @return Array of 16 bit uniform sound samples but stored not in an array of short
         * but in an array of bytes.
         */
        public  byte[] convert_a_law_to_16_bit_uniform_as_bytes(byte[] inArray) {
            byte[] outArray = new byte[(inArray.length * 2)];
            short thisResult = 0;
            int outIndex = 0;
            for (int i = 0; i < inArray.length; i = i + 1) {
                thisResult = convert_a_law_byte_to_16_bit_uniform(inArray[i]);
                // Now pack short into bytes - right way around?
                outArray[outIndex] = (byte) (thisResult & 0x000000FF);
                outArray[outIndex + 1] = (byte) ((thisResult >> 8) & 0x000000FF);
                outIndex = outIndex + 2;
            }
            return outArray;
        }


        /**
         * Convert an array or part of an array of A'law coded 8 bit samples
         * to 16 bit uniform samples embedded into a .wav wrapper. The result
         * is returned as a byte array to make it easy to convert to an
         * InputStream of bytes to send to the media player.
         * @param inArray  Array of A'law encoded 8 bit byte samples.
         * @param length  Length of the input array to process. This
         * allows part rather than whole arrays to be processed.
         * @return WAV coded media as an array of bytes.
         */
        public  byte[] convert_a_law_to_byte_wav(byte[] inArray, int length) {
            // Make array for result. 2 * input length as converting 8 bit samples
            // to 16 bit samples. Added 44 bytes for the WAV header data
            byte[] outArray = new byte[(length * 2) + 44];

            int L32 = length; // Number bytes in raw data

            try {
                // First 4 bytes are 'RIFF'
                outArray[0] = (byte) 0x52;
                outArray[1] = (byte) 0x49;
                outArray[2] = (byte) 0x46;
                outArray[3] = (byte) 0x46;

                // Next part is an odd encoding of the length of the data + 36

                int FL = 2 * L32 + 36;
                int MSW = FL >> 16;        // Most significant word
                int LSW = FL & 0x0000FFFF; // Least significant word

                // Split MSW and LSW as 16 bit values into bytes

                outArray[4] = (byte) (LSW & 0x000000FF);
                outArray[5] = (byte) ((LSW >> 8) & 0x000000FF);
                outArray[6] = (byte) (MSW & 0x000000FF);
                outArray[7] = (byte) ((MSW >> 9) & 0x000000FF);

                // Numbers in comments are 16 bit ints so 2 bytes each!
                // Can treat most of this as simply magic numbers

                // Bytes 8 - 11 'WAVE'
                outArray[8] = (byte) 0x57;
                outArray[9] = (byte) 0x41;
                outArray[10] = (byte) 0x56;
                outArray[11] = (byte) 0x45;

                // Bytes 12 - 15 'fmt '
                outArray[12] = (byte) 0x66;
                outArray[13] = (byte) 0x6d;
                outArray[14] = (byte) 0x74;
                outArray[15] = (byte) 0x20;

                // Bytes 16 - 19 = 16, 0,
                outArray[16] = (byte) 0x10;
                outArray[17] = (byte) 0x00;
                outArray[18] = (byte) 0x00;
                outArray[19] = (byte) 0x00;

                // Bytes 16 - 23 = 1, 1
                outArray[20] = (byte) 0x01;
                outArray[21] = (byte) 0x00;
                outArray[22] = (byte) 0x01;
                outArray[23] = (byte) 0x00;

                // Bytes 24 - 27 = 8000, 0

                outArray[24] = (byte) 0x40;
                outArray[25] = (byte) 0x1f;
                outArray[26] = (byte) 0x00;
                outArray[27] = (byte) 0x00;

                // Bytes 28 - 31 = 16000, 0

                outArray[28] = (byte) 0x80;
                outArray[29] = (byte) 0x3e;
                outArray[30] = (byte) 0x00;
                outArray[31] = (byte) 0x00;

                // Bytes 32 - 35 = 2, 16

                outArray[32] = (byte) 0x02;
                outArray[33] = (byte) 0x00;
                outArray[34] = (byte) 0x10;
                outArray[35] = (byte) 0x00;

                // Bytes 36 - 39 'data'
                outArray[36] = (byte) 0x64;
                outArray[37] = (byte) 0x61;
                outArray[38] = (byte) 0x74;
                outArray[39] = (byte) 0x61;

                // Repeat the length value like above but no added 36 this time

                FL = 2 * L32;
                MSW = FL >> 16;
                LSW = FL & 0x0000FFFF;

                outArray[40] = (byte) (LSW & 0x000000FF);
                outArray[41] = (byte) ((LSW >> 8) & 0x000000FF);
                outArray[42] = (byte) (MSW & 0x000000FF);
                outArray[43] = (byte) ((MSW >> 9) & 0x000000FF);

                // Now add the A'law converted to uniform
                byte wavRep [] = convert_a_law_to_16_bit_uniform_as_bytes(inArray);


                // Copy the result to the output array
                System.arraycopy(wavRep, 0,
                        outArray, 44,
                        (length * 2)); // now twice length
            } catch (Exception e) {
                e.printStackTrace();
            }
            return outArray;
        }

    }