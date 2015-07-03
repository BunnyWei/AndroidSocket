package com.example.mfbx3wh7.task22;

import java.lang.*;
import java.io.*;

public class bbsdvd08
/* Adapted from the following:*/ 
/* Copyright (c) 1999, 2001 Spectrum Applications, Derwood, MD, USA */
/* All rights reserved                                              */
/* Version 2.2 Last Modified 2001.11.28                             */

{
  public static final int K= 7;        /* constraint length */
  public static final int TWOTOTHEM = 64;      /* 2^(K - 1) */
  public static final double PI= 3.141592654;  
    
  static void deci2bin(int d, int size, int []b)
  {
    b[size-1] = d&0x01;
    for (int i = size - 2; i >= 0; i--) {
        d = d >> 1;
        b[i] = d & 0x01;
    }
  }
  static int  bin2deci(int []b, int size)
  {    
    int i, d=0;
    for (i = 0; i < size; i++)
        d += b[i] << (size - i - 1);
 
    return(d);
  }
  static int nxt_stat(int current_state, int input, int []memory_contents)
  {
    int[] binary_state = new int[K - 1];              /* binary value of current state */
    int[] next_state_binary =new int[K - 1];         /* binary value of next state */
    int next_state;                       /* decimal value of next state */
    int i;                                /* loop variable */

    /* convert the decimal value of the current state number to binary */
    deci2bin(current_state, K - 1, binary_state);

    /* given the input and current state number, compute the next state number */
    next_state_binary[0] = input;
    for (i = 1; i < K - 1; i++)
        next_state_binary[i] = binary_state[i - 1];
 
    /* convert the binary value of the next state number to decimal */
    next_state = bin2deci(next_state_binary, K - 1);

    /* memory_contents are the inputs to the modulo-two adders in the encoder */
    memory_contents[0] = input;
    for (i = 1; i < K; i++)
        memory_contents[i] = binary_state[i - 1];
 
    return(next_state);
  }
  

  static int soft_metric(int data, int guess)
  {
    return(Math.abs(data - (guess * 7)));
  }

  static short[]  bbsdvd(int[][] g, long channel_length, short[] soft_3bit_input, short[] decoder_output_matrix)
  {
    int i, j, l, ll;                          /* loop variables */
    long t;                                   /* time */
    int[] memory_contents = new int[K];                   /* input + conv. encoder sr */
    int[][] input = new int[TWOTOTHEM][TWOTOTHEM];          /* maps current/nxt sts to input */
    int[][] output = new int[TWOTOTHEM][2];                 /* gives conv. encoder output */
    int[][] nextstate = new int[TWOTOTHEM][2];              /* for current st, gives nxt given input */
    int [][]accum_err_metric = new int[TWOTOTHEM][2];       /* accumulated error metrics */
    int [][]state_history = new int[TWOTOTHEM][K * 5 + 1];  /* state history table */
    int []state_sequence = new int [K * 5 + 1];            /* state sequence list */
 
    int []channel_output_matrix;               /* ptr to input matrix */

    int[] binary_output = new int[2];                     /* vector to store binary enc output */
    int[] branch_output= new int[2];                     /* vector to store trial enc output */
 
    int m, n, number_of_states, depth_of_trellis, step, branch_metric,
        sh_ptr, sh_col, x, xx, h=0, hh, next_state, last_stop; /* misc variables */
 
/* ************************************************************************** */
 
    /* n is 2^1 = 2 for rate 1/2 */
    n = 2;
 
    /* m (memory length) = K - 1 */
    m = K - 1;

    /* number of states = 2^(K - 1) = 2^m for k = 1 */
    number_of_states = (int) Math.pow(2, m);

    /* little degradation in performance achieved by limiting trellis depth
       to K * 5--interesting to experiment with smaller values and measure
       the resulting degradation. */
    depth_of_trellis = K * 5;

    /* initialize data structures */
    for (i = 0; i < number_of_states; i++) {
        for (j = 0; j < number_of_states; j++)
            input[i][j] = 0;

        for (j = 0; j < n; j++) {
            nextstate[i][j] = 0;
            output[i][j] = 0;
        }

        for (j = 0; j <= depth_of_trellis; j++) {
            state_history[i][j] = 0;
        }

        /* initial accum_error_metric[x][0] = zero */
        accum_err_metric[i][0] = 0;
        /* by setting accum_error_metric[x][1] to MAXINT, we don't need a flag */
        /* so I don't get any more questions about this:  */
        /* MAXINT is simply the largest possible integer, defined in values.h */
        accum_err_metric[i][1] = Integer.MAX_VALUE;

    }

    /* generate the state transition matrix, output matrix, and input matrix
       - input matrix shows how FEC encoder bits lead to next state
       - next_state matrix shows next state given current state and input bit
       - output matrix shows FEC encoder output bits given current presumed
       encoder state and encoder input bit--this will be compared to actual
       received symbols to determine metric for corresponding branch of trellis
    */

    for (j = 0; j < number_of_states; j++) {
        for (l = 0; l < n; l++) {
            next_state = nxt_stat(j, l, memory_contents);
            input[j][next_state] = l;

            /* now compute the convolutional encoder output given the current
               state number and the input value */
            branch_output[0] = 0;
            branch_output[1] = 0;

            for (i = 0; i < K; i++) {
                branch_output[0] ^= memory_contents[i] & g[0][i];
                branch_output[1] ^= memory_contents[i] & g[1][i];
            }

            /* next state, given current state and input */
            nextstate[j][l] = next_state;
            /* output in decimal, given current state and input */
            output[j][l] = bin2deci(branch_output, 2);

        } /* end of l for loop */

    } /* end of j for loop */

    channel_output_matrix = new int[(int)channel_length];
    if (channel_output_matrix == null) 
    {
      // ERROR code
    }

    /* now we're going to rearrange the channel output so it has n rows,
       and n/2 columns where each row corresponds to a channel symbol for
       a given bit and each column corresponds to an encoded bit */
    channel_length = channel_length / n;

    /* interesting to compare performance of fixed vs adaptive quantizer */
    /* init_quantizer(); */
    //init_adaptive_quant(es_ovr_n0);
    /* quantize the channel output--convert float to short integer */
    /* channel_output_matrix = reshape(channel_output, n, channel_length) */
    for (t = 0; t < (channel_length * n); t += n) {
      for (i = 0; i < n; i++){
        j = soft_3bit_input[(int)t + i ];
        channel_output_matrix [(int)(t / n) + i * (int)channel_length ]  = j;
      }
    } /* end t for-loop */
 
/* ************************************************************************** */
 
    /* End of setup. Start decoding of channel outputs with forward
        traversal of trellis!  Stop just before encoder-flushing bits.  */
    for (t = 0; t < channel_length - m; t++) {

       if (t <= m)
           /* assume starting with zeroes, so just compute paths from all-zeroes state */
           step = (int)Math.pow(2, m - t * 1);
       else
           step = 1;

       /* we're going to use the state history array as a circular buffer so
          we don't have to shift the whole thing left after each bit is
          processed so that means we need an appropriate pointer */
        /* set up the state history array pointer for this time t */
       sh_ptr = (int) ( ( t + 1 ) % (depth_of_trellis + 1) );

       /* repeat for each possible state */
       for (j = 0; j < number_of_states; j+= step) {
           /* repeat for each possible convolutional encoder output n-tuple */
           for (l = 0; l < n; l++) {
               branch_metric = 0;

               /* compute branch metric per channel symbol, and sum for all
                   channel symbols in the convolutional encoder output n-tuple */
            
             
               /*==================================================================*/
               /* Next bit only works for n=2 (half rate), but it's fast! */

               /* convert the decimal representation of the encoder output to binary */
               binary_output[0] = ( output[j][l] & 0x00000002 ) >> 1;
               binary_output[1] = output[j][l] & 0x00000001;

               //TODO
                /* compute branch metric per channel symbol, and sum for all
                    channel symbols in the convolutional encoder output n-tuple */
                branch_metric = branch_metric + Math.abs( channel_output_matrix[(int)t]  - 7 * binary_output[0]) +
                                                Math.abs( channel_output_matrix[(1 * (int)channel_length + (int)t )] 
                                                - 7 * binary_output[1] );
             /*==================================================================*/

                /* now choose the surviving path--the one with the smaller accumlated
                    error metric... */
                if ( accum_err_metric[ nextstate[j][l] ] [1] > accum_err_metric[j][0] +
                    branch_metric ) {

                    /* save an accumulated metric value for the survivor state */
                    accum_err_metric[ nextstate[j][l] ] [1] = accum_err_metric[j][0] +
                        branch_metric;
 
                    /* update the state_history array with the state number of
                       the survivor */
                    state_history[ nextstate[j][l] ] [sh_ptr] = j;
 
                } /* end of if-statement */
            } /* end of 'l' for-loop */
        } /* end of 'j' for-loop -- we have now updated the trellis */
 
        /* for all rows of accum_err_metric, move col 2 to col 1 and flag col 2 */
        for (j = 0; j < number_of_states; j++) {
            accum_err_metric[j][0] = accum_err_metric[j][1];
            accum_err_metric[j][1] = Integer.MAX_VALUE;
        } /* end of 'j' for-loop */
 
 
        /* now start the traceback, if we've filled the trellis */
        if (t >= depth_of_trellis - 1) {
 
            /* initialize the state_sequence vector--probably unnecessary */
            for (j = 0; j <= depth_of_trellis; j++)
                state_sequence[j] = 0;
 
            /* find the element of state_history with the min. accum. error metric */
            /* since the outer states are reached by relatively-improbable runs
               of zeroes or ones, search from the top and bottom of the trellis in */
            x = Integer.MAX_VALUE;

            for (j = 0; j < ( number_of_states / 2 ); j++) {

                if ( accum_err_metric[j][0] < accum_err_metric[number_of_states - 1 - j][0] ) {
                    xx = accum_err_metric[j][0];
                    hh = j;
                }
                else {
                    xx = accum_err_metric[number_of_states - 1 - j][0];
                    hh = number_of_states - 1 - j;
                }
                if ( xx < x) {
                    x = xx;
                    h = hh;
                }

            } /* end 'j' for-loop */
 
       
 
            /* now pick the starting point for traceback */
            state_sequence[depth_of_trellis] = h;
 
            /* now work backwards from the end of the trellis to the oldest state
               in the trellis to determine the optimal path. The purpose of this
               is to determine the most likely state sequence at the encoder
                based on what channel symbols we received. */
            for (j = depth_of_trellis; j > 0; j--) {
                sh_col = j + ( sh_ptr - depth_of_trellis );
                if (sh_col < 0)
                   sh_col = sh_col + depth_of_trellis + 1;

                state_sequence[j - 1] = state_history[ state_sequence[j] ] [sh_col];
            } /* end of j for-loop */

            //TODO
            /* now figure out what input sequence corresponds to the state sequence
             in the optimal path */
            decoder_output_matrix[(int)t - depth_of_trellis + 1] =
                (short)input[state_sequence[0]] [state_sequence[1] ];

       } /* end of if-statement */
        
    } /* end of 't' for-loop */
 
/* ************************************************************************** */
 
    /* now decode the encoder flushing channel-output bits */
    for (t = channel_length - m; t < channel_length; t++) {

        /* set up the state history array pointer for this time t */
        sh_ptr = (int) ( ( t + 1 ) % (depth_of_trellis + 1) );

        /* don't need to consider states where input was a 1, so determine
         what is the highest possible state number where input was 0 */
        last_stop = number_of_states / (int)Math.pow(2, t - channel_length + m);

        /* repeat for each possible state */
        for (j = 0; j < last_stop; j++) {
 
            branch_metric = 0;
            deci2bin(output[j][0], n, binary_output);
 
            /* compute metric per channel bit, and sum for all channel bits
                in the convolutional encoder output n-tuple */
            for (ll = 0; ll < n; ll++) {
              // TODO
                branch_metric = branch_metric + soft_metric( channel_output_matrix[(int)(ll * channel_length + t)], binary_output[ll] );
            } /* end of 'll' for loop */

            /* now choose the surviving path--the one with the smaller total
                metric... */
            if ( (accum_err_metric[ nextstate[j][0] ][1] > accum_err_metric[j][0] +
                branch_metric) /*|| flag[ nextstate[j][0] ] == 0*/) {
 
                /* save a state metric value for the survivor state */
                accum_err_metric[ nextstate[j][0] ][1] = accum_err_metric[j][0] +
                    branch_metric;
 
                /* update the state_history array with the state number of
                    the survivor */
                state_history[ nextstate[j][0] ][sh_ptr] = j;
 
            } /* end of if-statement */
 
        } /* end of 'j' for-loop */
 
        /* for all rows of accum_err_metric, swap columns 1 and 2 */
        for (j = 0; j < number_of_states; j++) {
            accum_err_metric[j][0] = accum_err_metric[j][1];
            accum_err_metric[j][1] = Integer.MAX_VALUE;
        } /* end of 'j' for-loop */
 
        /* now start the traceback, if i >= depth_of_trellis - 1*/
        if (t >= depth_of_trellis - 1) {
 
            /* initialize the state_sequence vector */
            for (j = 0; j <= depth_of_trellis; j++) state_sequence[j] = 0;
 
            /* find the state_history element with the minimum accum. error metric */
            x = accum_err_metric[0][0];
            h = 0;
            for (j = 1; j < last_stop; j++) {
                if (accum_err_metric[j][0] < x) {
                    x = accum_err_metric[j][0];
                    h = j;
                } /* end if */
            } /* end 'j' for-loop */
 
            
 
            state_sequence[depth_of_trellis] = h;
 
            /* now work backwards from the end of the trellis to the oldest state
                in the trellis to determine the optimal path. The purpose of this
                is to determine the most likely state sequence at the encoder
                based on what channel symbols we received. */
            for (j = depth_of_trellis; j > 0; j--) {

                sh_col = j + ( sh_ptr - depth_of_trellis );
                if (sh_col < 0)
                   sh_col = sh_col + depth_of_trellis + 1;

                state_sequence[j - 1] = state_history[ state_sequence[j] ][sh_col];
            } /* end of j for-loop */

            /* now figure out what input sequence corresponds to the
                optimal path */
            
            // TODO
             decoder_output_matrix[(int)t - depth_of_trellis + 1] =
                (short)input[ state_sequence[0] ][ state_sequence[1] ];

       } /* end of if-statement */
    } /* end of 't' for-loop */
 
    for (i = 1; i < depth_of_trellis - m; i++)
      // TODO
        decoder_output_matrix[(int)channel_length - depth_of_trellis + i] =
            (short)input[ state_sequence[i] ] [ state_sequence[i + 1] ];

      return decoder_output_matrix;
  }
  public short[] executeVertical(int length,short[] encoded)
  {
    long    msg_length, channel_len; /* loop variables, length of I/O files */
    int     t, m, rtCount;
    short[]   sdvdout;            /* original, encoded, & decoded data arrays */
    short[]   soft_3bit_data;               /* input array to Viterbi decoder*/       
//    FileInputStream    fpIn;
//    FileOutputStream   fpOut;
//    DataInputStream IN;
//    DataOutputStream OUT;
    short temp;

/* Generator functions for IEEE convolutional coder:-  */  
    int [][]g = {{1,  1, 1, 1,  0, 0, 1}, /* 171 */
      {1,  0, 1, 1,  0, 1, 1}}; /* 133 */

//    if(args.length < 3)
//    {
//        System.out.println("There are not enough command arguments!\n ");
//        System.out.println("Command arguments are: bbsdvd.exe channel_length input_binary_file output_binary_file \n ");
//        System.exit(0);
//    }
 
    m = K - 1;
    channel_len =  new Integer(length).intValue();
    msg_length  =  channel_len /2 - m ;

    //encoded = new short[(int)channel_len];
    if (encoded == null) {
      // some error code
    }

    soft_3bit_data = new short[(int)channel_len];
    if (soft_3bit_data == null) {
      //some error code
    }

    sdvdout = new short[(int)msg_length];
    if (sdvdout == null) {
      // some error code
    }

//    try{
//      fpIn = new FileInputStream(inputFile);
//      IN = new DataInputStream(fpIn);
//      System.out.println("Input file: " + inputFile);
//      //TODO
//      for(int i=0; i < channel_len; i++){
//        temp= IN.readShort();
//        encoded[(int)i] = temp;
//      }
//      IN.close();
//    }
//    catch (Exception e)
//    {
//      System.out.println("Can not open input file" +inputFile);
//      System.exit(0);
//    }

    
    /* now transform the data from 0/1 to 0/7 */
    for (t = 0; t < channel_len; t++) {
        /*if the binary data value is 1, the channel symbol is '111' = 7; if the
        binary data value is 0, the channel symbol is 000 = 0. */ 
        //soft_3bit_data[t] = (short)(7 * encoded[t]/256);
        soft_3bit_data[t] = (short)(7 * encoded[t]);
     }
    
    sdvdout = bbsdvd(g, channel_len, soft_3bit_data, sdvdout);
    System.out.println("channel length = "+ channel_len); 
    System.out.println("output length = "+ msg_length);
      return sdvdout;
    
//    try{
//      fpOut = new FileOutputStream(outputFile);
//
//      System.out.println("Output file: " + outputFile);
//      OUT = new DataOutputStream(fpOut);
//      for(int i = 0; i <msg_length; i++){
//        OUT.writeShort(sdvdout[i]*256);
//      }
//      OUT.close();
//    }
//    catch (Exception e)
//    {
//      System.out.println("Can not open output file" +outputFile);
//      System.exit(0);
//    }
    
}
  
}