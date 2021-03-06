/*
Copyright or © or Copr. Jason Mahdjoub (04/02/2016)

jason.mahdjoub@distri-mind.fr

This software (Utils) is a computer program whose purpose is to give several kind of tools for developers 
(ciphers, XML readers, decentralized id generators, etc.).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */

package com.distrimind.util.harddrive;

import com.distrimind.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since Utils 1.0
 * @see HardDriveDetect
 */

class LinuxHardDriveDetect extends UnixHardDriveDetect {
	private Set<Disk> disks;
	private Set<Partition> partitions;

	LinuxHardDriveDetect() {

	}

	@Override
	Set<Disk> getDetectedDisksImpl() {
		return disks;
	}

	@Override
	Set<Partition> getDetectedPartitionsImpl() {
		return partitions;
	}

	private List<String[]> scanUUIDs() throws IOException {
		List<String[]> scannedUUIDs=new ArrayList<>();
		Process p=Runtime.getRuntime().exec(new String[]{"blkid"});
		try (InputStreamReader isr = new InputStreamReader(p.getInputStream())) {
			try (BufferedReader br = new BufferedReader(isr)) {
				String line;
				while ((line=br.readLine())!=null)
				{
					scannedUUIDs.add(line.split(" "));
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally {
			Utils.flushAndDestroyProcess(p);
		}

		return scannedUUIDs;
	}

	@Override
	void scanDisksAndPartitions() {
		disks=new HashSet<>();
		partitions=new HashSet<>();




		HashMap<String, Disk> disksString=new HashMap<>();

		File file = new File("/proc/mounts");
		try (FileInputStream fis = new FileInputStream(file)) {
			try (InputStreamReader isr = new InputStreamReader(fis)) {
				try (BufferedReader br = new BufferedReader(isr)) {
					List<String[]> scannedUUIDs=scanUUIDs();
					String line = br.readLine();
					while (line != null) {
						String[] values = line.split(" ");
						if (values.length > 2) {
							String node = values[0];
							if (node.startsWith("/dev/") && !node.startsWith("/dev/loop")) {
								try {
									String originalNode=node;
									node = new File(node).getCanonicalPath();
									if (node.endsWith("/"))
										node=node.substring(0, node.length()-1);
									String disk=originalNode;
									if (disk.startsWith("/dev/sd")) {
										char last_char = disk.charAt(disk.length() - 1);
										while (last_char >= '0' && last_char <= '9') {
											disk = disk.substring(0, disk.length() - 1);
											if (disk.length() > 0) {
												last_char = disk.charAt(disk.length() - 1);
											} else
												break;
										}
									}
									else if (disk.startsWith("/dev/nvme")) {
										char last_char = disk.charAt(disk.length() - 1);
										while (last_char >= '0' && last_char <= '9') {
											disk = disk.substring(0, disk.length() - 1);
											if (disk.length() > 0) {
												last_char = disk.charAt(disk.length() - 1);
											} else
												break;
										}
										disk=disk.substring(0, disk.length()-1);
									}

									int li=node.lastIndexOf("/")+1;
									String nodeShort=node.substring(li);
                                    li=disk.lastIndexOf("/")+1;
                                    String diskNodeShort=disk.substring(li);

									if (disk.length() > 1) {
                                        Disk d=disksString.get(disk);
									    if (d==null)
                                        {
                                            d=new Disk(null, getDiskOrPartitionSize(diskNodeShort), !isRemovable(diskNodeShort, node), -1, getProtocol(disk), disk, null);
                                            disks.add(d);
                                            disksString.put(disk, d);
                                        }
                                        File mountPoint=new File(values[1]);
                                        Partition p=new Partition(getPartitionUUID(scannedUUIDs, nodeShort, originalNode),mountPoint, node, values[2], values[2], -1, mountPoint.canWrite(), getPartitionLabel(nodeShort), getDiskOrPartitionSize(nodeShort), d);
                                        partitions.add(p);
									}
								} catch (IOException e) {
								    e.printStackTrace();
								}
							}
						}
						line = br.readLine();
					}
				}
			}
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

	private long getDiskOrPartitionSize(String diskOrPartition)  {
		File file = new File("/sys/class/block/"+diskOrPartition+"/size");
		
		if (file.exists()) {
			
			long size=-1;
			try (FileInputStream fis = new FileInputStream(file)) {
				try (InputStreamReader isr = new InputStreamReader(fis)) {
					try (BufferedReader br = new BufferedReader(isr)) {
						size=Long.parseLong(br.readLine());
						
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return size;
		}
		else
			return -1;
	}
	private boolean isRemovable(String disk, String node)  {
		File file = new File("/sys/class/block/"+disk+"/removable");
		if (file.exists()) {
			boolean removable;
			try (FileInputStream fis = new FileInputStream(file)) {
				try (InputStreamReader isr = new InputStreamReader(fis)) {
					try (BufferedReader br = new BufferedReader(isr)) {
						removable=Integer.parseInt(br.readLine())==1;
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return node.startsWith("/media") || node.startsWith("/mnt");
			}
			return removable;
		}
		else
			return node.startsWith("/media") || node.startsWith("/mnt");
	}
	private static final String emptyUUID="00000000-0000-0000-0000-000000000000";
	private UUID getPartitionUUID(List<String[]> scannedUUIDs, String nodeShort, String originalNode) throws IOException {
		for (String[] line : scannedUUIDs)
		{
			if (line[0].equals(originalNode+":"))
			{
				try {
					return UUID.fromString(line[1].substring(6, line[1].length()-1));
				}
				catch (IllegalArgumentException ignored)
				{

				}
			}
		}


	    String r= getCorrespondence("/dev/disk/by-partuuid", nodeShort);

	    if (r==null)
	    	return null;
	    else {
			if (r.length()<emptyUUID.length())
				r=r+emptyUUID.substring(r.length());
			try {
				return UUID.fromString(r);
			}
			catch (IllegalArgumentException ignored)
			{
				return null;
			}

		}
    }
    private String getPartitionLabel(String nodeShort) throws IOException {
	    String res= getCorrespondence("/dev/disk/by-partlabel", nodeShort);
	    if (res==null)
	        res= getCorrespondence("/dev/disk/by-label", nodeShort);
	    return res;
    }
    private String getProtocol(String nodeShort) throws IOException {
        return getCorrespondence("/dev/disk/by-path", nodeShort);
    }
    private String getCorrespondence(String searchPath, String nodeShort) throws IOException {
        String path="../../"+nodeShort;

        Process p = Runtime.getRuntime().exec(new String[]{"ls","-g","-o","--time-style=+",searchPath});
        try (InputStreamReader isr = new InputStreamReader(p.getInputStream())) {
            try (BufferedReader br = new BufferedReader(isr)) {
                String line;
                while ((line=br.readLine())!=null)
                {
					String[] cols = line.split(" ");
                    if (cols.length==7 && cols[6].equals(path))
                    {
                        return cols[4];
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally {
			Utils.flushAndDestroyProcess(p);
		}
        return null;
    }

    public static void main(String[] args)
	{
		try {
			Partition p=HardDriveDetect.getInstance().getConcernedPartition(new File("/"));
			Disk disk=p.getDisk();
			System.out.println(p);
			System.out.println(disk);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
