package implementation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.eac.RSAPublicKey;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.SubjectDirectoryAttributes;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.Arrays.Iterator;
import org.bouncycastle.util.CollectionStore;
import org.bouncycastle.util.Store;
import org.bouncycastle.x509.extension.X509ExtensionUtil;
import code.GuiException;
import gui.Constants;
import gui.GuiInterfaceV1;

public class MyCode extends x509.v3.CodeV3 {
	
	private KeyStore myKeystore;

	public MyCode(boolean[] algorithm_conf, boolean[] extensions_conf, boolean extensions_rules) throws GuiException {
		super(algorithm_conf, extensions_conf, extensions_rules);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean canSign(String keypair_name) {
		try {
			boolean bool = myKeystore.containsAlias(keypair_name);			
			if(bool == false)return false;
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		try {
			X509Certificate certificate = (X509Certificate) myKeystore.getCertificate(keypair_name);
			int CAconstant = certificate.getBasicConstraints();
			if(CAconstant == -1)return false;
			return true;
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}

	@Override
	public boolean exportCSR(String file, String keypair_name, String algorithm) {
		
		
			FileOutputStream ostream = null;
	
		try {
			if(myKeystore.containsAlias(keypair_name)) {
				X509Certificate certificate = (X509Certificate) myKeystore.getCertificate(keypair_name);
				X509CertificateHolder holder = new JcaX509CertificateHolder(certificate);
				
				/*if(holder.getSubject() != holder.getIssuer()) {
					GuiInterfaceV1.reportError("Zeljeni sertifikat je vec potpisan.");
					return false;
				}*///Verovatno ne moze != za stringove
				if (!holder.getSubject().equals(holder.getIssuer())) {
					GuiInterfaceV1.reportError("Zeljeni sertifikat je vec potpisan.");
					return false;
				}
				PublicKey public_key = certificate.getPublicKey();
				PrivateKey private_key = (PrivateKey) myKeystore.getKey(keypair_name, null);
				JcaContentSignerBuilder signer_builder = new JcaContentSignerBuilder(algorithm).setProvider("BC");
				ContentSigner signer = signer_builder.build(private_key);
				PKCS10CertificationRequestBuilder req_builder = new JcaPKCS10CertificationRequestBuilder(holder.getSubject(), public_key);
				PKCS10CertificationRequest request = req_builder.build(signer);
				ostream = new FileOutputStream(file);
				ostream.write(request.getEncoded());
				
				ostream.close();
				return true;
			}
			else return false;
		} catch (KeyStoreException | CertificateEncodingException | UnrecoverableKeyException | NoSuchAlgorithmException | OperatorCreationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean exportCertificate(String file, String keypair_name, int encoding, int format) {
		FileOutputStream ostream = null;
		JcaPEMWriter pemwriter = null;
		FileWriter regularwriter = null;
	try {
		if(format == Constants.HEAD) {
			Certificate certificate = myKeystore.getCertificate(keypair_name);
			
			if(encoding == Constants.DER) {
				ostream = new FileOutputStream(file);
				ostream.write(certificate.getEncoded());
			}
			else if(encoding == Constants.PEM) {
				regularwriter = new FileWriter(file);
				pemwriter = new JcaPEMWriter(regularwriter);
				pemwriter.writeObject(certificate);
			}
			
		}
		else if (format == Constants.CHAIN) {
			regularwriter = new FileWriter(file);
			pemwriter = new JcaPEMWriter(regularwriter);
			if(myKeystore.isKeyEntry(keypair_name)) {
				Certificate[] certchain = myKeystore.getCertificateChain(keypair_name);
				for(int i=0; i<certchain.length;i++)
					pemwriter.writeObject(certchain[i]);
			}
			else if(myKeystore.isCertificateEntry(keypair_name)) {
				Certificate c = myKeystore.getCertificate(keypair_name);
				pemwriter.writeObject(c);
			}
		}
	}catch(Exception e) {return false;}
	
	try {
		ostream.close();
		pemwriter.close();
		regularwriter.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return false;
	}
	return true;
	
	}

	@Override
	public boolean exportKeypair(String keypair_name, String file, String password) {
		FileOutputStream ostream = null;
		try {
			KeyStore tempkeystore = KeyStore.getInstance("PKCS12", "BC");
			tempkeystore.load(null,null);
			Key tempkey = myKeystore.getKey(keypair_name, null);
			Certificate[] tempchain = myKeystore.getCertificateChain(keypair_name);
			tempkeystore.setKeyEntry(keypair_name, tempkey, null, tempchain);
			ostream = new FileOutputStream(file);
			tempkeystore.store(ostream, "password".toCharArray());
			
		}catch(Exception e){return false;}
		
		try {
			ostream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
		
	}

	@Override
	public String getCertPublicKeyAlgorithm(String keypair_name) {
		X509Certificate certificate = null;
		String algorithm=null;
		try {
			boolean bool = myKeystore.containsAlias(keypair_name);
			if(!bool)return null;
			X509Certificate cer = (X509Certificate) myKeystore.getCertificate(keypair_name);
			algorithm = cer.getPublicKey().getAlgorithm();
			if(algorithm != null)return algorithm;
			else return null;
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getCertPublicKeyParameter(String keypair_name) {
		
		try {
			if(!myKeystore.containsAlias(keypair_name))return null;
			X509Certificate c = (X509Certificate) myKeystore.getCertificate(keypair_name);
			PublicKey public_key = c.getPublicKey();
			
			if(public_key instanceof ECPublicKey) {
				ECPublicKey ECkey = (ECPublicKey) public_key;
				ECParameterSpec ec_par_spec = ECkey.getParameters();
				for(@SuppressWarnings("rawtypes")
				Enumeration names = ECNamedCurveTable.getNames();names.hasMoreElements();) {
					final String name = (String)names.nextElement();

			        final X9ECParameters params = ECNamedCurveTable.getByName(name);

			        if (params.getN().equals(ec_par_spec.getN())
			            && params.getH().equals(ec_par_spec.getH())
			            && params.getCurve().equals(ec_par_spec.getCurve())
			            && params.getG().equals(ec_par_spec.getG())){
			            return name;
			        }
				}
				return null;
				}
			else return null;
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getSubjectInfo(String keypair_name) {
		String returnString = null;
		String errorRetString="Sertifikat sa zeljenim imenom ne postoji u keystore-u.";
		try {
			boolean bool=myKeystore.containsAlias(keypair_name);
			if(bool) {
				X509CertificateHolder holder = new JcaX509CertificateHolder((X509Certificate) myKeystore.getCertificate(keypair_name));
				returnString = holder.getSubject().toString();
				return returnString;
			}
			else return errorRetString;
		} catch (KeyStoreException | CertificateEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Exception";
		}
	}

	@Override
	public boolean importCAReply(String file, String keypair_name) {
		FileOutputStream ostream = null;
		try {
		Path path = Paths.get(file);
		byte[] byte_array = Files.readAllBytes(path);
		CMSSignedData signed_data = new CMSSignedData(byte_array);
		Store<X509CertificateHolder> temp = signed_data.getCertificates();
		Collection<X509CertificateHolder> certificate_collection = temp.getMatches(null);
		Certificate[] chain = new Certificate[2];
		
		for (X509CertificateHolder holder : certificate_collection) {
			X509Certificate cert = new JcaX509CertificateConverter().getCertificate(holder);

			if (cert.getBasicConstraints() == -1)
				chain[0] = cert;
			else
				chain[1] = cert;
		}
		X509CertificateHolder certHolder = new JcaX509CertificateHolder((X509Certificate) chain[0]);

		/*int i=0;
		Iterator<X509CertificateHolder> iterator = (Iterator<X509CertificateHolder>) certificate_collection.iterator();
		while(iterator.hasNext()) {
			X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(iterator.next());
			chain[i] = certificate;
			i++;
		}
		X509Certificate cert = (X509Certificate) myKeystore.getCertificate(keypair_name);
		X509CertificateHolder cert_holder = new JcaX509CertificateHolder(cert);*/
		
		
		PublicKey public_key = chain[0].getPublicKey();
		PrivateKey private_key = (PrivateKey) myKeystore.getKey(keypair_name, null);
		
		X509Certificate selectedCert = (X509Certificate) myKeystore.getCertificate(keypair_name);
		X509CertificateHolder selectedCertHolder = new JcaX509CertificateHolder(selectedCert);
		boolean bool1 = selectedCertHolder.getSubject().equals(selectedCertHolder.getIssuer());
		boolean bool2= public_key.equals(selectedCert.getPublicKey());
		if(!bool1) {
			GuiInterfaceV1.reportError("Zeljeni sertifikat je vec potpisan.");
			return false;
		}
		if(!bool2) {
			GuiInterfaceV1.reportError("CAReply iz fajla sa zadatom putanjom ne odgovara sertifikatu sa zadatim aliasom.");
			return false;
		}
		
		
		myKeystore.deleteEntry(keypair_name);
		myKeystore.setKeyEntry(keypair_name, private_key, null, chain);
		ostream = new FileOutputStream("myKeystore.p12");
		myKeystore.store(ostream, "password".toCharArray());
		
	
	   
		}catch(Exception e) {return false;}
		try {
			ostream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public String importCSR(String file) {
		Path path_to_file = Paths.get(file);
		String ret = null;
		try {
			JcaPKCS10CertificationRequest request = new JcaPKCS10CertificationRequest(Files.readAllBytes(path_to_file));
			CertificateSigningRequestPK = request.getPublicKey();
			ret = request.getSubject().toString();
			return ret;
		} catch (IOException | InvalidKeyException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean importCertificate(String file, String keypair_name) {
		FileInputStream istream = null;
		FileOutputStream ostream = null;
		
		try {
			CertificateFactory certGen = CertificateFactory.getInstance("X.509", "BC");
			istream = new FileInputStream(file);
			Certificate certificate = certGen.generateCertificate(istream);
			myKeystore.setCertificateEntry(keypair_name, certificate);
			ostream = new FileOutputStream("myKeystore.p12");
			myKeystore.store(ostream, "password".toCharArray());
		} catch (CertificateException | NoSuchProviderException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		try {
			istream.close();
			ostream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
		
		
				
		
	}

	@Override
	public boolean importKeypair(String keypair_name, String file, String password) {
		File fullFile = new File(file);
		try(FileOutputStream ostream = new FileOutputStream("myKeystore.p12");FileInputStream istream = new FileInputStream(fullFile)){
			KeyStore store = KeyStore.getInstance("PKCS12", "BC");
			store.load(istream,password.toCharArray());
			Enumeration<String> aliases = store.aliases();
			String ime = aliases.nextElement();
			Key key = store.getKey(ime, password.toCharArray());
			Certificate[] certchain = store.getCertificateChain(ime);
			myKeystore.setKeyEntry(keypair_name, key,null, certchain);
			myKeystore.store(ostream,"password".toCharArray());
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public int loadKeypair(String keypair_name) {
		try {
			System.out.println(keypair_name);
			boolean bool = false;
			bool = myKeystore.containsAlias(keypair_name);
			if (bool == false) {System.out.println("Kaze da ga nema u storu");return -1;}
			System.out.println("proverio da li postoji u storeu");
			X509Certificate certificate = (X509Certificate)myKeystore.getCertificate(keypair_name);
			System.out.println("tralalal");
			X509CertificateHolder certificate_holder = new X509CertificateHolder(certificate.getEncoded());
			int certificate_version = 0;
			if(certificate.getVersion() == 3)certificate_version = Constants.V3;
			else GuiInterfaceV1.reportError("Aplikacijom je podrzana samo verzija 3 sertifikata.");
			/*if(certificate_version != Constants.V3) {
				GuiInterfaceV1.reportError("Aplikacijom je podrzana samo verzija 3 sertifikata.");
				return -1;
			}*/
			System.out.println("Doso do postavljanja na gui");
			access.setVersion(certificate_version);
			access.setNotAfter(certificate_holder.getNotAfter());
			access.setNotBefore(certificate_holder.getNotBefore());
			access.setPublicKeyAlgorithm(certificate.getPublicKey().getAlgorithm());
			access.setSerialNumber(certificate_holder.getSerialNumber().toString());
			access.setPublicKeyDigestAlgorithm(certificate.getSigAlgName().replace("WITH", "with"));
			PublicKey public_key = certificate.getPublicKey();
			/*if(public_key instanceof ECPublicKey) { //OVAJ DEO PROVERI!!!
				ECPublicKey eckey = (ECPublicKey) certificate.getPublicKey();
				//access.setPublicKeyParameter(Integer.toString(eckey.getParameters().getN().bitLength()));
				//access.setPublicKeyParameter(eckey.getParameters().);
			}
			if (certificate.getPublicKey() instanceof RSAPublicKey) {
				RSAPublicKey rsakey = (RSAPublicKey) certificate.getPublicKey();
				access.setPublicKeyParameter(Integer.toString(rsakey.getModulus().bitLength()));
			}*/
			if(public_key instanceof RSAPublicKey) {
				RSAPublicKey rsakey = (RSAPublicKey)public_key;
				access.setPublicKeyParameter(Integer.toString(rsakey.getModulus().bitLength()));
			}
			if(public_key instanceof ECPublicKey) {
				ECPublicKey eckey = (ECPublicKey)public_key;
				access.setPublicKeyParameter(getCertPublicKeyParameter(keypair_name));
				
			}
			X500Name subject_name = certificate_holder.getSubject();
			String ime = subject_name.toString();
			access.setSubject(ime);
			access.setSubjectSignatureAlgorithm(certificate.getPublicKey().getAlgorithm());
			
			X500Name issuer = certificate_holder.getIssuer();
			System.out.print("Stigo do ekst");
			///////EKSTENZIJE DODAJ/////
			Extensions extensions = certificate_holder.getExtensions();
			
			//Subject Key Identifier
			Extension extension = certificate_holder.getExtension(Extension.subjectKeyIdentifier);
			if(extension!= null) {
				byte[] ext_val = certificate.getExtensionValue(Extension.subjectKeyIdentifier.getId());
				@SuppressWarnings("deprecation")
				SubjectKeyIdentifier id = SubjectKeyIdentifier.getInstance(X509ExtensionUtil.fromExtensionValue(ext_val));
				byte[] keyID = id.getKeyIdentifier();
				access.setSubjectKeyID(keyID.toString());
			}
			
			//Basic Constraints
			Extension e = certificate_holder.getExtension(Extension.basicConstraints);
			if(e != null) {
				int path_length = certificate.getBasicConstraints();
				if(path_length != -1) {
					access.setCA(true);
					access.setPathLen(path_length + "");
				}
			}
			//Subject Directory Attributes
			Extension ext = extensions.getExtension(Extension.subjectDirectoryAttributes);
			if(ext != null) {
				String date_of_birth=null;
				String gender=null;
				String city=null;
				String place_of_birth=null;
				SubjectDirectoryAttributes sda = SubjectDirectoryAttributes.getInstance(ext.getParsedValue());
				@SuppressWarnings("unchecked")
				Vector<Attribute> vector = sda.getAttributes();
				for (Attribute attribute : vector) {
					ASN1ObjectIdentifier type = attribute.getAttrType();
					if (type.equals(BCStyle.PLACE_OF_BIRTH)) {
						ASN1Set set = attribute.getAttrValues();
						DERBitString derbitstringset = (DERBitString) set.iterator().next();
						place_of_birth = new String(derbitstringset.getBytes());
					}
					if (type.equals(BCStyle.COUNTRY_OF_CITIZENSHIP)) {
						ASN1Set set = attribute.getAttrValues();
						DERBitString derbitstringset = (DERBitString) set.iterator().next();
						city = new String(derbitstringset.getBytes());
					}
					if (type.equals(BCStyle.GENDER)) {
						ASN1Set set = attribute.getAttrValues();
						DERBitString derbitstringset = (DERBitString) set.iterator().next();
						gender = new String(derbitstringset.getBytes());
					}
					if (type.equals(BCStyle.DATE_OF_BIRTH)) {
						ASN1Set set = attribute.getAttrValues();
						DERBitString derbitstringset = (DERBitString) set.iterator().next();
						date_of_birth = new String(derbitstringset.getBytes());
					}
				}
				access.setGender(gender);
				access.setDateOfBirth(date_of_birth);
				access.setSubjectDirectoryAttribute(Constants.POB, place_of_birth);
				access.setSubjectDirectoryAttribute(Constants.COC, city);
				access.setCritical(Constants.SDA, ext.isCritical());
				
			}
			
			//////KRAJ EKSTENZIJA/////
			
			
			
			
			/*if(!subject_name.equals(issuer)) {
				access.setIssuer(issuer.toString());
				access.setIssuerSignatureAlgorithm(certificate.getSigAlgName());
				return 1;
			}
			if(subject_name.equals(issuer))return 0;
			if(myKeystore.isCertificateEntry(keypair_name)) {
				if(subject_name.equals(issuer) == false) {
					access.setIssuer(issuer.toString());
					access.setIssuerSignatureAlgorithm(certificate.getSigAlgName());
					return 2;
				}
			}*/
			if (myKeystore.isCertificateEntry(keypair_name)) {
				if (!subject_name.equals(issuer)) {
					access.setIssuer(issuer.toString());
					access.setIssuerSignatureAlgorithm(certificate.getSigAlgName());
				}
				return 2;
			}
			if (subject_name.equals(issuer)) {
				return 0;
			} else {
				access.setIssuer(issuer.toString());
				access.setIssuerSignatureAlgorithm(certificate.getSigAlgName());
				return 1;
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
		
	}

	@Override
	public Enumeration<String> loadLocalKeystore() {
		FileInputStream inputstream = null;
		FileOutputStream outputstream = null;
		
		try {
			Security.addProvider(new BouncyCastleProvider());
			myKeystore = KeyStore.getInstance("PKCS12", "BC");
			try {
				inputstream = new FileInputStream("myKeystore.p12");
				myKeystore.load(inputstream, "password".toCharArray());
			}catch(FileNotFoundException e) {
				myKeystore.load(null,null);
				outputstream = new FileOutputStream("myKeystore.p12");
				myKeystore.store(outputstream, "password".toCharArray());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally{
			try {
			if(inputstream != null)
				inputstream.close();
			if(outputstream != null)
				outputstream.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			return myKeystore.aliases();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		return null; // ako baci exception
	
	}

	@Override
	public boolean removeKeypair(String keypair_name) {
		FileOutputStream ostream = null;
		
		try {
			ostream = new FileOutputStream("myKeystore.p12");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		try {
			
			myKeystore.deleteEntry(keypair_name);
			
			myKeystore.store(ostream,"password".toCharArray());
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		finally {
			if(ostream!=null)
				try {
					ostream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
		}
		return true;
		
		
	}

	@Override
	public void resetLocalKeystore() {
		FileOutputStream ostream = null;
		try {
			ostream = new FileOutputStream("myKeystore.p12");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			KeyStore.getInstance("PKCS12","BC");
			myKeystore.load(null,null);
			
			myKeystore.store(ostream, "password".toCharArray());
			
			
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			ostream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		return;
		
	}

	@Override
	public boolean saveKeypair(String keypair_name) {
		FileOutputStream ostream = null;
		int cert_version = access.getVersion();
		if(cert_version != Constants.V3) {
			GuiInterfaceV1.reportError("Aplikacija podrzava samo verziju 3 sertifikata.");
			return false;
		}
		String algorithm = access.getPublicKeyAlgorithm();
		if(algorithm != "EC") {
			GuiInterfaceV1.reportError("Aplikacija podrzava samo EC algoritam.");
			return false;
		}
		try {
			boolean bool= myKeystore.containsAlias(keypair_name);
			if(bool) {
				GuiInterfaceV1.reportError("Zeljeni alias vec postoji u keystore-u.");
				return false;
			}
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
			ECKeyPairGenerator generator = new ECKeyPairGenerator();
			//X9ECParameters params = SECNamedCurves.getByName("prime256v1");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime256v1");
			
			 //ECDomainParameters domainParams = new ECDomainParameters(params.getCurve(),
                     //params.getG(), params.getN(), params.getH(),
                    // params.getSeed());
			 //ECKeyGenerationParameters keyGenParams = new ECKeyGenerationParameters(domainParams, new SecureRandom());
			//generator.init(keyGenParams);
			 //AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();
			String name = access.getSubject();
			Date notBefore = access.getNotBefore();
			Date notAfter = access.getNotAfter();
			X500Name subject_name = new X500Name(name);
			BigInteger serial_number = new BigInteger(access.getSerialNumber());
			//int keylength = Integer.parseInt(access.getPublicKeyParameter());
			String digest_algorithm = access.getPublicKeyDigestAlgorithm();
			
			keyGen.initialize(ecSpec);
			KeyPair key_pair = keyGen.generateKeyPair();
			PublicKey public_key = (PublicKey) key_pair.getPublic();
			PrivateKey private_key = (PrivateKey) key_pair.getPrivate();
			
			X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(subject_name, serial_number, notBefore, notAfter,
					subject_name, public_key);
			
			ContentSigner content_signer = new JcaContentSignerBuilder(digest_algorithm).setProvider("BC").build(private_key);
			//ContentSigner signer = new JcaContentSignerBuilder(algorithm).setProvider("BC").build(private_key);
			/////EKSTENZIJE DODAJ/////
			
			///Subject Key Identifier
			SubjectKeyIdentifier ski;
			if (access.isCritical(Constants.SKID)) {
				GuiInterfaceV1.reportError("Ova ekstenzija ne moze biti critical.");
				return false;
			}
			ski = new SubjectKeyIdentifier(public_key.getEncoded());
			builder.addExtension(Extension.subjectKeyIdentifier,access.isCritical(Constants.SDA),ski);
			
			
			///Basic Constraints
			BasicConstraints basic_constraints_field;
			if (access.isCA())
				basic_constraints_field = new BasicConstraints(Integer.parseInt(access.getPathLen()));
			else
				basic_constraints_field = new BasicConstraints(false);
			builder.addExtension(Extension.basicConstraints, access.isCritical(Constants.BC), basic_constraints_field);
			
			///SubjectDirectoryAttributes
			Vector<Attribute> att = new Vector<Attribute>();
			String date_of_birth = access.getDateOfBirth();
			String gender = access.getGender();
			String pob = access.getSubjectDirectoryAttribute(Constants.POB);
			String coc = access.getSubjectDirectoryAttribute(Constants.COC);
			
			if(date_of_birth.isEmpty() == false) {
				att.add(new Attribute(BCStyle.DATE_OF_BIRTH, new DERSet(new DERBitString(date_of_birth.getBytes()))));
			}
			if(gender.isEmpty() == false) {
				att.add(new Attribute(BCStyle.GENDER, new DERSet(new DERBitString(gender.getBytes()))));
			}
			if(pob.isEmpty() == false) {
				att.add(new Attribute(BCStyle.PLACE_OF_BIRTH, new DERSet(new DERBitString(pob.getBytes()))));
			}
			if(coc.isEmpty() == false) {
				att.add(new Attribute(BCStyle.COUNTRY_OF_CITIZENSHIP,new DERSet(new DERBitString(coc.getBytes()))));
			}
			if(att.isEmpty() == false) {
				SubjectDirectoryAttributes attributes = new SubjectDirectoryAttributes(att);
				
				if (access.isCritical(Constants.SDA)) {
					GuiInterfaceV1.reportError("Ova ekstenzija ne moze biti critical.");//Proveri ovo, pisalo negde na netu
					return false;
				}
				builder.addExtension(Extension.subjectDirectoryAttributes, access.isCritical(Constants.SDA), attributes);
			}
			
			/////KRAJ EKSTENZIJA/////
			X509CertificateHolder cert_holder = builder.build(content_signer);
			X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(cert_holder);
			
			ostream = new FileOutputStream("myKeystore.p12");
			
			X509Certificate[] certificate_chain = new X509Certificate[1];
			certificate_chain[0] = certificate;
			
			myKeystore.setKeyEntry(keypair_name, private_key,null, certificate_chain);
			myKeystore.store(ostream, "password".toCharArray());
			System.out.printf("Uneta verzija sertifikata %d",certificate.getVersion() );
			
			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		finally {
			if(ostream!=null)
				try {
					ostream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return true;
			
	}

	@Override
	public boolean signCSR(String file, String keypair_name, String algorithm) {
		BigInteger serial_number;
		Date not_before,not_after;
		X500Name subject_name;
		FileOutputStream ostream = null;
		try {
			X509Certificate certificate_CA = (X509Certificate) myKeystore.getCertificate(keypair_name);
			X509CertificateHolder CAholder = new JcaX509CertificateHolder(certificate_CA);
			serial_number = new BigInteger(access.getSerialNumber());
			not_before = access.getNotBefore();
			not_after = access.getNotAfter();
			subject_name = new X500Name(access.getSubject());
			X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(CAholder.getSubject(), serial_number, not_before, not_after,
					subject_name, CertificateSigningRequestPK);
			ContentSigner signer = new JcaContentSignerBuilder(algorithm).setProvider("BC")
					.build((PrivateKey) myKeystore.getKey(keypair_name, null));
			
			/////EKSTENZIJE OVDE DODAJ/////
			
			///Subject Key Identifier
			PublicKey pk = CertificateSigningRequestPK;
			SubjectKeyIdentifier ski;
			if (access.isCritical(Constants.SKID)) {
				GuiInterfaceV1.reportError("Ova ekstenzija ne moze biti critical.");
				return false;
			}
			ski = new SubjectKeyIdentifier(pk.getEncoded());
			certGen.addExtension(Extension.subjectKeyIdentifier,access.isCritical(Constants.SDA),ski);
			
			///SubjectDirectoryAttributes
			Vector<Attribute> att = new Vector<Attribute>();
			String date_of_birth = access.getDateOfBirth();
			String gender = access.getGender();
			String pob = access.getSubjectDirectoryAttribute(Constants.POB);
			String coc = access.getSubjectDirectoryAttribute(Constants.COC);
			
			if(date_of_birth.isEmpty() == false) {
				att.add(new Attribute(BCStyle.DATE_OF_BIRTH, new DERSet(new DERBitString(date_of_birth.getBytes()))));
			}
			if(gender.isEmpty() == false) {
				att.add(new Attribute(BCStyle.GENDER, new DERSet(new DERBitString(gender.getBytes()))));
			}
			if(pob.isEmpty() == false) {
				att.add(new Attribute(BCStyle.PLACE_OF_BIRTH, new DERSet(new DERBitString(pob.getBytes()))));
			}
			if(coc.isEmpty() == false) {
				att.add(new Attribute(BCStyle.COUNTRY_OF_CITIZENSHIP,new DERSet(new DERBitString(coc.getBytes()))));
			}
			if(att.isEmpty() == false) {
				SubjectDirectoryAttributes attributes = new SubjectDirectoryAttributes(att);
				
				if (access.isCritical(Constants.SDA)) {
					GuiInterfaceV1.reportError("Ova ekstenzija ne moze biti critical.");//Proveri ovo, pisalo negde na netu
					return false;
				}
				certGen.addExtension(Extension.subjectDirectoryAttributes, access.isCritical(Constants.SDA), attributes);
			}
			
			///Basic Constraints
			BasicConstraints basic_constraints_field;
			if (access.isCA())
				basic_constraints_field = new BasicConstraints(Integer.parseInt(access.getPathLen()));
			else
				basic_constraints_field = new BasicConstraints(false);
			certGen.addExtension(Extension.basicConstraints, access.isCritical(Constants.BC), basic_constraints_field);
			
			/////KRAJ EKSENZIJA/////
			X509CertificateHolder signerHolder = certGen.build(signer);
			
			
			CMSSignedDataGenerator signed_data_Gen = new CMSSignedDataGenerator();
			signed_data_Gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
					new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()).build(signer,
							(X509Certificate) certificate_CA));
			ArrayList<X509CertificateHolder> list = new ArrayList<>();
			list.add(signerHolder);
			//list.add(CAholder);
			Certificate[] chain = myKeystore.getCertificateChain(keypair_name);
			for(Certificate cert: chain)
				list.add(new JcaX509CertificateHolder((X509Certificate) cert));
			CollectionStore<X509CertificateHolder> store = new CollectionStore<>(list);
			signed_data_Gen.addCertificates(store);

			CMSTypedData CMS_data = new CMSProcessableByteArray(new X500Name(access.getSubject()).getEncoded());
			CMSSignedData signedData = signed_data_Gen.generate(CMS_data, true);

			ostream = new FileOutputStream(file);
			ostream.write(signedData.getEncoded());
			
			
		}catch(Exception e) {return false;}
		CertificateSigningRequestPK = null;
		try {
			ostream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
		
	}
	private PublicKey CertificateSigningRequestPK;

}
