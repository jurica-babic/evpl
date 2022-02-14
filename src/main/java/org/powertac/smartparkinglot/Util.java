package org.powertac.smartparkinglot;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

import org.jfree.data.time.Hour;
import org.powertac.smartparkinglot.exception.HourlyValuesResolveException;

import sim.util.TableLoader;

public class Util {

	public static double[][] getHourlyValuesForAYear(String file, double defaultEntry, DataGranularity granularity)
			throws HourlyValuesResolveException {

		double[][] result = new double[2][Simulation.HOURS_PER_YEAR];

		double[][] temp = new double[2][1];
		temp[0][0] = 1;
		temp[1][0] = defaultEntry;

		try {

			temp = loadTextFile(Util.class.getResourceAsStream(file));
		} catch (NullPointerException e) {
			if (defaultEntry != -1) {
				System.err.println("A file " + file + " for is not found, will use a default value: " + defaultEntry);
			} else {
				throw new HourlyValuesResolveException("A file " + file + " for is not found", e);
			}
		} catch (IOException e) {
			if (defaultEntry != -1) {
				System.err.println(
						"Problem with loading of a text file " + file + ", will use a default value: " + defaultEntry);
			} else {
				throw new HourlyValuesResolveException("Problem with loading of a text file " + file, e);
			}
		}

		if (temp[1].length == 1) {
			granularity = DataGranularity.SINGLE_VALUE;
		}

		checkDataGranularity(temp[1], granularity);

		switch (granularity) {
		case SINGLE_VALUE: {
			for (int i = 0; i < Simulation.HOURS_PER_YEAR; i++) {
				result[0][i] = i;
				result[1][i] = defaultEntry;
			}
			break;
		}
		case DAILY: {
			int n = 0;
			for (int i = 0; i < temp[1].length; i++) {
				for (int j = 0; j < 24; j++) {
					result[0][n] = n;
					result[1][n] = temp[1][i];
					n++;
				}
			}
			break;
		}
		case HOURLY: {
			result = temp;
		}
			break;

		default:
			break;
		}

		return result;
	};

	private static void checkDataGranularity(double[] temp, DataGranularity granularity)
			throws HourlyValuesResolveException {
		switch (granularity) {
		case DAILY:
			if (temp.length != Simulation.DAYS_PER_YEAR) {
				throw new HourlyValuesResolveException("Data is not daily.");
			}
			break;
		case HOURLY:
			if (temp.length != Simulation.HOURS_PER_YEAR) {
				throw new HourlyValuesResolveException("Data is not hourly.");
			}
			break;
		case SINGLE_VALUE:
			if (temp.length != 1) {
				throw new HourlyValuesResolveException("Data is not a single value.");
			}
			break;
		default:
			break;
		}

	}

	/**
	 * Copy from MASON {@link TableLoader}, in this version we only force Locale
	 * US to avoid trouble with decimal points (i.e., we expect files which use
	 * '.' as decimal separator)
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public static double[][] loadTextFile(InputStream stream) throws IOException {
		Scanner scan = new Scanner(stream);

		ArrayList rows = new ArrayList();
		int width = -1;

		while (scan.hasNextLine()) {
			String srow = scan.nextLine().trim();
			if (srow.length() > 0) {
				int w = 0;
				if (width == -1) // first time compute width
				{
					ArrayList firstRow = new ArrayList();
					Scanner rowScan = new Scanner(new StringReader(srow)).useLocale(Locale.US);
					while (rowScan.hasNextDouble()) {
						firstRow.add(new Double(rowScan.nextDouble())); // ugh,
																		// boxed
						w++;
					}
					width = w;
					double[] row = new double[width];
					for (int i = 0; i < width; i++)
						row[i] = ((Double) (firstRow.get(i))).doubleValue();
					rows.add(row);
				} else {
					double[] row = new double[width];
					Scanner rowScan = new Scanner(new StringReader(srow)).useLocale(Locale.US);

					while (rowScan.hasNextDouble()) {
						if (w == width) // uh oh
							throw new IOException("Row lengths do not match in text file");
						row[w] = rowScan.nextDouble();
						w++;
					}
					if (w < width) // uh oh
						throw new IOException("Row lengths do not match in text file");
					rows.add(row);
				}
			}
		}

		if (width == -1) // got nothing
			return new double[0][0];

		double[][] fieldTransposed = new double[rows.size()][];
		for (int i = 0; i < rows.size(); i++)
			fieldTransposed[i] = ((double[]) (rows.get(i)));

		// now transpose because we have width first
		double[][] field = new double[width][fieldTransposed.length];
		for (int i = 0; i < field.length; i++)
			for (int j = 0; j < field[i].length; j++)
				field[i][j] = fieldTransposed[j][i];

		return field;
	}
	
	public static int getModulatedTimeIndex(double time){
		return (int) Math.floor(time) % Simulation.HOURS_PER_YEAR;
	}

}
